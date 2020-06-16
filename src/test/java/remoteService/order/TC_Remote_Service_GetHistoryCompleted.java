package remoteService.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import model.Catalog;
import model.User;

public class TC_Remote_Service_GetHistoryCompleted extends TestBase {
	private User user = new User();
	private String[] phoneNumbers = new String[11];
	private Catalog catalog = new Catalog();
	private String testCase;
	private String userId;
	private String page;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_GetHistoryCompleted() {
		
	}
	
	public TC_Remote_Service_GetHistoryCompleted(String testCase, String userId, String page, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.page = page;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void getHistoryCompletedRemoteService(String userId, String page) {
		logger.info("Call Get Get History Completed API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("page:" + page);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", "getHistoryCompleted");
		requestParams.put("message", "{\"userId\":" + userId + ",\"page\":" + page + "}");
		
		RestAssured.baseURI = URIOrder;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.POST, "/api/test/");
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (userId.equals("true")) {
			isCreateUser = true;
			
			// initialize user
			user.setName("Zanuar");
			user.setEmail("triromadon@gmail.com");
			user.setUsername("081252930398");
			user.setPin(123456);
			
			// delete if exist
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());
			
			// insert user into database
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));			
			userId = Long.toString(user.getId());

			// insert balance into database
			createBalance(user.getId(), 10000000);
			
			// initialize catalog - TELKOMSEL 15k
			catalog.setId(13);
			catalog.setProviderId(2);
			catalog.setValue(15000);
			catalog.setPrice(15000);
			
			// insert transaction into database
			if (testCase.equals("Valid user id and page (below 10 history)")) {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 1);
				phoneNumbers[0] = user.getUsername();
				
			} else if (testCase.equals("Valid user id and page (more than 10 history)")) {

				for (int i = 0; i < 11; i++) {
					createTransaction(user.getId(), "08125216179" + Integer.toString(i), catalog.getId(), 1);
					phoneNumbers[i] = "08125216179" + Integer.toString(i);
				}
			}
		}
	}
	
	@Test
	public void testHistoryCompleted() {
		getHistoryCompletedRemoteService(userId, page);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testHistoryCompleted"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.equals("unknown user") && !responseBody.equals("invalid request format")) {
			if (!response.getBody().asString().equals("[]")) {
				List<HashMap<Object, Object>> history = response.jsonPath().get();
				
				Assert.assertTrue(history.size() <= 10, "maximum history per page is only 10");
				
				for (int i = 0; i < history.size(); i++) {
					Assert.assertNotNull(history.get(i).get("id"));

					if (history.size() > 1) {
						Assert.assertEquals(history.get(i).get("phoneNumber"), phoneNumbers[history.size() - i]);
					} else {
						Assert.assertEquals(history.get(i).get("phoneNumber"), user.getUsername());						
					}
					
					Assert.assertEquals(history.get(i).get("price"),catalog.getPrice());
					Assert.assertNull(history.get(i).get("voucher"));
					Assert.assertEquals(history.get(i).get("status"), "COMPLETED");
					Assert.assertNotNull(history.get(i).get("createdAt"));
				}
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("[]")) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE userId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("unknown user")) {
			try {
				Connection conn = getConnectionMember();
				String queryString = "SELECT * FROM user WHERE id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		} else if (responseBody.equals("invalid request format")) {
			// do some code
			
		} else {
			List<HashMap<Object, Object>> history = response.jsonPath().get();
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT "
						+ "A.id, "
						+ "A.phoneNumber, "
						+ "A.createdAt, "
						+ "B.price, "
						+ "C.name "
						+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
						+ "LEFT JOIN transaction_status C on A.statusId = C.id "
						+ "WHERE A.userId = ? ORDER BY A.createdAt DESC LIMIT 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no history found in database");
				}
				do {
					int index = rs.getRow() - 1;
					Assert.assertEquals(history.get(index).get("id"), rs.getString("id"));
					Assert.assertEquals(history.get(index).get("phoneNumber"), rs.getString("phoneNumber"));
					Assert.assertEquals(history.get(index).get("price"), rs.getString("price"));
					Assert.assertEquals(history.get(index).get("status"), rs.getString("status"));
//					Assert.assertEquals(history.get(index).get("createdAt"), rs.getLong("createdAt"));
				} while(rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		// delete user
		if (isCreateUser == true) {
			deleteTransactionByUserId(user.getId());
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
