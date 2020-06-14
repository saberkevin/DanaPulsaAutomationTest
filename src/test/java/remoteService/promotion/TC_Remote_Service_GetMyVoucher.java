package remoteService.promotion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

public class TC_Remote_Service_GetMyVoucher extends TestBase {
	private User user = new User();
	private String userId;
	private String page;
	
	public TC_Remote_Service_GetMyVoucher() {
		
	}
	
	public TC_Remote_Service_GetMyVoucher(String userId, String page) {
		this.userId = userId;
		this.page = page;
	}

	@SuppressWarnings("unchecked")
	public void getMyVoucherRemoteService(String userId, String page) {
		logger.info("Call Get My Voucher API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("page:" + page);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", "getMyVoucher");
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
	public void testMyVouchers() {
		// call API my voucher remote service
		getMyVoucherRemoteService(userId, page);
	}
	
	@Test(dependsOnMethods = {"testMyVouchers"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			if (!response.getBody().asString().equals("[]")) {
				List<Map<String, String>> vouchers = response.jsonPath().get();
				
				for (int i = 0; i < vouchers.size(); i++) {
					Assert.assertNotNull(vouchers.get(i).get("id"));
					Assert.assertNotNull(vouchers.get(i).get("name"));
					Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
					Assert.assertNotNull(vouchers.get(i).get("discount"));
					Assert.assertNotNull(vouchers.get(i).get("maxDeduction"));
					Assert.assertNotNull(vouchers.get(i).get("filePath"));
					Assert.assertNotNull(vouchers.get(i).get("expiryDate"));
				}
			}
		} else if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "invalid user id");
		} else if (statusCode == 404) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "404");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "invalid page");
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			if (response.getBody().asString().equals("[]")) {
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT "
							+ "A.id, "
							+ "B.name AS voucherName, "
							+ "B.discount "
							+ "C.name AS voucherTypeName, "
							+ "B.maxDeduction "
							+ "B.filePath, "
							+ "B.expiryDate "
							+ "FROM user_voucher A LEFT JOIN voucher B on A.voucherId = B.id "
							+ "LEFT JOIN voucher_type C on B.typeId = C.id "
							+ "WHERE A.voucherStatusId != 1 AND B.isActive = true AND A.userId = ? LIMIT ?, 10";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, user.getId());
					ps.setInt(2, (Integer.parseInt(page)-1) * 10);
					
					ResultSet rs = ps.executeQuery();
					Assert.assertTrue(!rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				List<Map<String, String>> vouchers = response.jsonPath().get();
				
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT "
							+ "A.id, "
							+ "B.name AS voucherName, "
							+ "B.discount "
							+ "C.name AS voucherTypeName, "
							+ "B.maxDeduction "
							+ "B.filePath, "
							+ "B.expiryDate "
							+ "FROM user_voucher A LEFT JOIN voucher B on A.voucherId = B.id "
							+ "LEFT JOIN voucher_type C on B.typeId = C.id "
							+ "WHERE A.voucherStatusId != 1 AND B.isActive = true AND A.userId = ? LIMIT ?, 10";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, user.getId());
					ps.setInt(2, (Integer.parseInt(page)-1) * 10);
					
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						int index = rs.getRow() - 1;
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("id")), rs.getString("id"));
						Assert.assertEquals(vouchers.get(index).get("name"), rs.getString("voucherName"));
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("discount")), rs.getString("discount"));
						Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), rs.getString("voucherTypeName"));
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("maxDeduction")), rs.getString("maxDeduction"));
						Assert.assertEquals(vouchers.get(index).get("filePath"), rs.getString("filePath"));
//							Assert.assertEquals(vouchers.get(index).get("expiryDate"), rs.getLong("expiryDate"));
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
