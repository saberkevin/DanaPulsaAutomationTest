package remoteService.promotion;

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

public class TC_Remote_Service_Unredeem extends TestBase {
	private User user = new User();
	private String description;
	private String userId;
	private String voucherId;
	
	public TC_Remote_Service_Unredeem() {
		
	}
	
	public TC_Remote_Service_Unredeem(String description, String userId, String voucherId) {
		this.description = description;
		this.userId = userId;
		this.voucherId = voucherId;
	}
	
	@SuppressWarnings("unchecked")
	public void getUnredeemRemoteService(String userId, String voucherId) {
		logger.info("Call Unredeem API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("voucher id:" + voucherId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", ConfigRemoteServicePromotion.QUEUE_UNREDEEM);
		requestParams.put("request", "{\"userId\":" + userId + ",\"voucherId\":" + voucherId + "}");
		
		RestAssured.baseURI = ConfigRemoteServicePromotion.BASE_URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.GET, ConfigRemoteServicePromotion.ENDPOINT_PATH);
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + description);

		// initialize user
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setUsername("081252930398");
		user.setPin(123456);
		
		// insert user into database and get user id from it
		deleteUserIfExist(user.getEmail(), user.getUsername());
		createUser(user);
		user.setId(getUserIdByUsername(user.getUsername()));
		
		// if data from excel "true", then get valid user id
		if (userId.equals("true")) {
			userId = Long.toString(user.getId());
		}
	}
	
	@Test
	public void testUnredeem() {
		// call API unreedem voucher
		getUnredeemRemoteService(userId, voucherId);
		
		int statusCode = response.getStatusCode();

		if (statusCode != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testUnredeem"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		
		if (!responseBody.equals("unknown voucher") && !responseBody.equals("invalid request format")) {
			Assert.assertNotNull(response.getBody().jsonPath().get("id"));
			Assert.assertNotNull(response.getBody().jsonPath().get("name"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("unknown provider")) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM user_voucher WHERE userId = ? AND voucherId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				ps.setLong(2, Long.parseLong(voucherId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("invalid request format")) {
			// do some code
			
		} else {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM user_voucher WHERE userId = ? AND voucherId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				ps.setLong(2, Long.parseLong(voucherId));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no voucher data");
				}
				do {
					Assert.assertEquals(response.getBody().jsonPath().getLong("id"), rs.getLong("id"));
					Assert.assertEquals(response.getBody().jsonPath().getString("name"), rs.getString("name"));
				} while(rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
