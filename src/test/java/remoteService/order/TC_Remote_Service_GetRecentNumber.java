package remoteService.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import model.User;

public class TC_Remote_Service_GetRecentNumber extends TestBase {
	private User user = new User();
	private String userId;
	
	public TC_Remote_Service_GetRecentNumber() {
		
	}
	
	public TC_Remote_Service_GetRecentNumber(String userId) {
		this.userId = userId;
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
	public void testRecentNumber() {
		// call API get promotion vouchers
		getRecentNumberRemoteService(userId);
		
		int statusCode = response.getStatusCode();

		if (statusCode != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testRecentNumber"})
	public void checkData() throws ParseException {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			String responseBody = response.getBody().asString();
			
			if (!responseBody.equals("unknown user") && !responseBody.equals("invalid request format")) {
				if (!response.getBody().asString().equals("[]")) {
					List<Map<String, String>> vouchers = response.jsonPath().get();
					
					for (int i = 0; i < vouchers.size(); i++) {
						Assert.assertNotNull(vouchers.get(i).get("number"));
						Assert.assertNotNull(vouchers.get(i).get("provider.id"));
						Assert.assertNotNull(vouchers.get(i).get("provider.name"));
						Assert.assertNotNull(vouchers.get(i).get("provider.image"));
						Assert.assertNotNull(vouchers.get(i).get("date"));
					}
				}
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			if (response.getBody().asString().equals("[]")) {
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT * FROM transaction WHERE userId = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, user.getId());
					
					ResultSet rs = ps.executeQuery();
					Assert.assertTrue(!rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				List<Map<String, String>> vouchers = response.jsonPath().get();
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT A.phoneNumber, A.createdAt, B.* "
							+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
							+ "LEFT JOIN provider C on B.providerId = C.id "
							+ "WHERE A.userId = ? ORDER BY createdAt DESC LIMIT 10";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, user.getId());
					
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						int index = rs.getRow() - 1;
						Assert.assertEquals(vouchers.get(index).get("number"), rs.getString("phoneNumber"));
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("provider.id")), rs.getString("id"));						
						Assert.assertEquals(vouchers.get(index).get("provider.name"), rs.getString("name"));						
						Assert.assertEquals(vouchers.get(index).get("provider.image"), rs.getString("image"));						
//							Assert.assertEquals(vouchers.get(index).get("date"), rs.getLong("createdAt"));
					}
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
