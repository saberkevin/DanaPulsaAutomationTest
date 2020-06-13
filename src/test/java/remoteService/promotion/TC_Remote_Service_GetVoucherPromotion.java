package remoteService.promotion;

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

public class TC_Remote_Service_GetVoucherPromotion extends TestBase {
	private User user = new User();
	private String userId;
	private String page;
	
	@SuppressWarnings("unchecked")
	public void getPromotionVoucherRemoteService(String userId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("page:" + page);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", "getVoucherPromotion");
		requestParams.put("request", "{\"userId\":" + userId + ",\"page\":" + page + "}");
		
		RestAssured.baseURI = URIPromotion;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, "/test");
	}
	
	@BeforeClass
	public void beforeClass() {
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
//		if (userId.equals("true")) {
//			userId = Long.toString(user.getId());
//		}
		
		//hard code for testing
		userId = Long.toString(user.getId());
	}
	
	@Test
	public void testPromotionVouchers() {
		// call API promotion voucher remote service
//		getPromotionVoucherRemoteService(userId, page);
		getPromotionVoucherRemoteService(userId, "1");
		System.out.println(response.getBody().asString());
	}
	
	@Test(dependsOnMethods = {"testPromotionVouchers"})
	public void checkData() throws ParseException {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			List<Map<String, String>> promotionVouchers = response.jsonPath().get();
			
			if(promotionVouchers != null) {
				for (int i = 0; i < promotionVouchers.size(); i++) {
					System.out.println(promotionVouchers.get(i).get("name"));
					
					Assert.assertNotNull(promotionVouchers.get(i).get("id"));
					Assert.assertNotNull(promotionVouchers.get(i).get("name"));
					Assert.assertNotNull(promotionVouchers.get(i).get("voucherTypeName"));
					Assert.assertNotNull(promotionVouchers.get(i).get("filePath"));
					Assert.assertNotNull(promotionVouchers.get(i).get("expiryDate"));
				}
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			List<Map<String, String>> promotionVouchers = response.jsonPath().get();
			
			if(promotionVouchers != null) {				
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT A.id, A.name AS voucherName, B.name AS voucherTypeName, A.filePath, A.expiryDate "
							+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						System.out.println(rs.getRow());

						Assert.assertEquals(rs.getString("id"), promotionVouchers.get(rs.getRow()).get("id"));
						Assert.assertEquals(rs.getString("voucherName"), promotionVouchers.get(rs.getRow()).get("name"));
						Assert.assertEquals(rs.getString("voucherTypeName"), promotionVouchers.get(rs.getRow()).get("voucherTypeName"));
						Assert.assertEquals(rs.getString("filePath"), promotionVouchers.get(rs.getRow()).get("filePath"));
						Assert.assertEquals(rs.getString("expiryDate"), promotionVouchers.get(rs.getRow()).get("expiryDate"));
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
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
