package remoteService.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import model.User;

public class TC_Remote_Service_CreateTransaction extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String phoneNumber;
	private String catalogId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_CreateTransaction() {
		
	}
	
	public TC_Remote_Service_CreateTransaction(String testCase, String userId, String phoneNumber, String catalogId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.phoneNumber = phoneNumber;
		this.catalogId = catalogId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void createTransactionRemoteService(String userId, String phoneNumber, String catalogId) {
		logger.info("Call Create Transaction API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("phone number:" + phoneNumber);
		logger.info("catalog id:" + catalogId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", "createTransaction");
		requestParams.put("message", "{\"userId\":" + userId + ",\"phoneNumber\":\"" + phoneNumber + "\",\"catalogId\":" + catalogId + "}");
		
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
		}
	}
	
	@Test
	public void testCreateTransaction() {
		createTransactionRemoteService(userId, phoneNumber, catalogId);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testCreateTransaction"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.equals("unknown user")) {
			// do some code

		} else if (!responseBody.equals("you’ve already requested this exact order within the last 30 seconds, "
				+ "please try again later if you actually intended to do that")) {
			// do some code

		} else if (!responseBody.equals("catalog not found")) {
			// do some code

		} else if (!responseBody.equals("selected catalog is not available for this phone’s provider")) {
			// do some code

		} else if (!responseBody.equals("unknown phone number")) {
			// do some code

		} else if (!responseBody.equals("invalid phone number")) {
			// do some code

		} else if (!responseBody.equals("invalid request format")) {
			// do some code

		} else {
			Assert.assertNotNull(response.getBody().jsonPath().get("id"));
			Assert.assertEquals(response.getBody().jsonPath().get("phoneNumber"), phoneNumber);
			Assert.assertEquals(response.getBody().jsonPath().get("catalog.id"), catalogId);
			Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.id"));
			Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.name"));
			Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.image"));
			Assert.assertNotNull(response.getBody().jsonPath().get("catalog.value"));
			Assert.assertNotNull(response.getBody().jsonPath().get("catalog.price"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("unknown user")) {
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
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				ps.setString(2, phoneNumber);
				ps.setLong(3, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("you’ve already requested this exact order within the last 30 seconds, "
				+ "please try again later if you actually intended to do that")) {
			// do some code
			
		} else if (responseBody.equals("catalog not found")) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM pulsa_catalog WHERE id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				ps.setString(2, phoneNumber);
				ps.setLong(3, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("selected catalog is not available for this phone’s provider")) {
			String providerName = "";
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT name FROM provider WHERE id IN (SELECT providerId FROM provider_prefix WHERE prefix = ?)";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setString(1, phoneNumber.substring(1,5));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no provider found in database");
				}
				providerName = rs.getString("name");
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT B.name "
						+ "FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id "
						+ "WHERE A.id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
					Assert.assertTrue(false, "no provider found in database");
				}
				Assert.assertNotEquals(rs.getString("name"), providerName);
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				ps.setString(2, phoneNumber);
				ps.setLong(3, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("unknown phone number") 
				|| responseBody.equals("invalid phone number") 
				|| responseBody.equals("invalid request format")) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setString(1, userId);
				ps.setString(2, phoneNumber);
				ps.setString(3, catalogId);
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		} else {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT COUNT(*) AS count FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				ps.setString(2, phoneNumber);
				ps.setLong(3, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
					Assert.assertTrue(false, "no transaction found in database");
				}
				do {
					Assert.assertEquals(rs.getInt("count"), 1);
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
		
		// delete transaction
		deleteTransactionByPhoneNumber(phoneNumber);			
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
