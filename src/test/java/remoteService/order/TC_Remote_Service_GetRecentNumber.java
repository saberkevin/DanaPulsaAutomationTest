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
import model.Provider;
import model.User;

public class TC_Remote_Service_GetRecentNumber extends TestBase {
	private User user = new User();
	private String[] phoneNumbers = new String[11];
	private Provider provider = new Provider();
	private String testCase;
	private String userId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_GetRecentNumber() {
		
	}
	
	public TC_Remote_Service_GetRecentNumber(String testCase, String userId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void getRecentNumberRemoteService(String userId) {
		logger.info("Call Get Recent Number API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", "getRecentNumber");
		requestParams.put("message", userId);
		
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
			
			// insert user into database
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));
			
			userId = Long.toString(user.getId());

			// insert balance into database
			createBalance(user.getId(), 10000000);
						
			// initialize provider - TELKOMSEL
			provider.setId(2);
			provider.setName("Telkomsel");
			provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
			
			// insert transaction into database
			if (testCase.equals("Valid ID (below 10 transaction history)")) {
				createTransaction(user.getId(), user.getUsername(), 13);
				phoneNumbers[0] = user.getUsername();
				
			} else if (testCase.equals("Valid ID (more than 10 transaction history)")) {

				for (int i = 0; i < 11; i++) {
					createTransaction(user.getId(), "08125216179" + Integer.toString(i), 13);
					phoneNumbers[i] = "08125216179" + Integer.toString(i);
				}
			}
		}
	}
	
	@Test
	public void testRecentNumber() {
		getRecentNumberRemoteService(userId);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"testRecentNumber"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.equals("unknown user") && !responseBody.equals("invalid request format")) {
			if (!response.getBody().asString().equals("[]")) {
				List<HashMap<Object, Object>> recentNumbers = response.jsonPath().get();
				
				Assert.assertTrue(recentNumbers.size() <= 10, "maximum recent number is only 10");
				
				for (int i = 0; i < recentNumbers.size(); i++) {

					if (recentNumbers.size() > 1) {
						Assert.assertEquals(recentNumbers.get(i).get("number"), phoneNumbers[recentNumbers.size() - i]);
					} else {
						Assert.assertEquals(recentNumbers.get(i).get("number"), user.getUsername());						
					}

					HashMap<String, String> provHashMap = (HashMap<String, String>) recentNumbers.get(i).get("provider");
					Assert.assertEquals(String.valueOf(provHashMap.get("id")), Long.toString(provider.getId()));
					Assert.assertEquals(provHashMap.get("name"), provider.getName());
					Assert.assertEquals(provHashMap.get("image"), provider.getImage());
					
					Assert.assertNotNull(recentNumbers.get(i).get("date"));
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
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
			// do some code
			
		} else if (responseBody.equals("invalid request format")) {
			// do some code
			
		} else {
			List<HashMap<Object, Object>> recentNumbers = response.jsonPath().get();
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT A.phoneNumber, A.createdAt, C.* "
						+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
						+ "LEFT JOIN provider C on B.providerId = C.id "
						+ "WHERE A.userId = ? ORDER BY A.createdAt DESC LIMIT 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no transaction found in database");
				}
				do {
					int index = rs.getRow() - 1;
					Assert.assertEquals(recentNumbers.get(index).get("number"), rs.getString("phoneNumber"));
					
					HashMap<String, String> provHashMap = (HashMap<String, String>) recentNumbers.get(index).get("provider");
					Assert.assertEquals(String.valueOf(provHashMap.get("id")), rs.getString("id"));						
					Assert.assertEquals(provHashMap.get("name"), rs.getString("name"));						
					Assert.assertEquals(provHashMap.get("image"), rs.getString("image"));						

//					Assert.assertEquals(recentNumbers.get(index).get("date"), rs.getLong("createdAt"));
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
