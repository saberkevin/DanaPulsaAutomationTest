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
	
	public TC_Remote_Service_GetVoucherPromotion(String userId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		this.userId = userId;
		this.page = page;
	}

	@SuppressWarnings("unchecked")
	public void getPromotionVoucherRemoteService(String userId, String page) {
		logger.info("Call Get Promotion Voucher API [Promotion Domain]");
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
		logger.info(response.getBody().asString());
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
		if (userId.equals("true")) {
			userId = Long.toString(user.getId());
		}
	}
	
	@Test
	public void testPromotionVouchers() {
		// call API get promotion vouchers
		getPromotionVoucherRemoteService(userId, page);
	}
	
	@Test(dependsOnMethods = {"testPromotionVouchers"})
	public void checkData() throws ParseException {
		
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			List<Map<String, String>> vouchers = response.jsonPath().get();
			
			if(vouchers != null) {
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT "
							+ "A.id, "
							+ "A.name AS voucherName, "
							+ "B.name AS voucherTypeName, "
							+ "A.filePath, "
							+ "A.expiryDate "
							+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id LIMIT ? WHERE ";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setInt(1, Integer.parseInt(page) * 10);
					
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						int index = rs.getRow() - 1;
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("id")), rs.getString("id"));
						Assert.assertEquals(vouchers.get(index).get("name"), rs.getString("voucherName"));						
						Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), rs.getString("voucherTypeName"));						
						Assert.assertEquals(vouchers.get(index).get("filePath"), rs.getString("filePath"));						
//						Assert.assertEquals(vouchers.get(index).get("expiryDate"), rs.getLong("expiryDate"));
						
						System.out.print(String.valueOf(vouchers.get(index).get("expiryDate")) + " - ");
						System.out.println(rs.getLong("expiryDate"));
					}
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (statusCode == 404) {
				
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
