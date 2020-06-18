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
	private String testCase;
	private String userId;
	private String page;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_GetMyVoucher() {
		
	}
	
	public TC_Remote_Service_GetMyVoucher(String testCase, String userId, String page, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.page = page;
		this.result = result;
		isCreateUser = false;
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
			
			// insert voucher into database
			if (testCase.equals("Valid user id and page (below 10 vouchers)")) {	
				
				createUserVoucher(user.getId(), 1, 2);	

			} else if (testCase.equals("Valid user id and page (more than 10 vouchers)")) {
			
				for (int i = 0; i < 11; i++) {		
					createUserVoucher(user.getId(), i + 1, 2);			
				}
			}
		}
	}
	
	@Test
	public void testMyVouchers() {
		getMyVoucherRemoteService(userId, page);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testMyVouchers"})
	public void checkData() {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.contains("invalid request format") 
				&& !responseBody.equals("user not found")
				&& !responseBody.equals("you don’t have any vouchers")) {
			List<Map<String, String>> vouchers = response.jsonPath().get();

			Assert.assertTrue(vouchers.size() <= 10, "maximum vouchers per page is 10");
			
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
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("you don’t have any vouchers") || responseBody.equals("[]")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.voucherId, "
						+ "B.name AS voucherName, "
						+ "B.discount, "
						+ "C.name AS voucherTypeName, "
						+ "B.maxDeduction, "
						+ "B.filePath, "
						+ "B.expiryDate "
						+ "FROM user_voucher A LEFT JOIN voucher B on A.voucherId = B.id "
						+ "LEFT JOIN voucher_type C on B.typeId = C.id "
						+ "WHERE A.voucherStatusId != 1 AND B.isActive = 1 AND A.userId = ? "
						+ "ORDER BY A.voucherId ASC LIMIT ?, 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1,Long.parseLong(userId));
				ps.setInt(2, (Integer.parseInt(page)-1) * 10);
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.contains("user not found")) {
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
		} else if (responseBody.contains("invalid request format")) {
			// do some code
			
		} else {
			List<Map<String, String>> vouchers = response.jsonPath().get();
			
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.voucherId, "
						+ "B.name AS voucherName, "
						+ "B.discount, "
						+ "C.name AS voucherTypeName, "
						+ "B.maxDeduction, "
						+ "B.filePath, "
						+ "B.expiryDate "
						+ "FROM user_voucher A LEFT JOIN voucher B on A.voucherId = B.id "
						+ "LEFT JOIN voucher_type C on B.typeId = C.id "
						+ "WHERE A.voucherStatusId != 1 AND B.isActive = 1 AND A.userId = ? "
						+ "ORDER BY A.voucherId ASC LIMIT ?, 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1,Long.parseLong(userId));
				ps.setInt(2, (Integer.parseInt(page)-1) * 10);
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no voucher found in database");
				}
				do {
					int index = rs.getRow() - 1;
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("id")), rs.getString("voucherId"));
					Assert.assertEquals(vouchers.get(index).get("name"), rs.getString("voucherName"));
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("discount")), rs.getString("discount"));
					Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), rs.getString("voucherTypeName"));
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("maxDeduction")), rs.getString("maxDeduction"));
					Assert.assertEquals(vouchers.get(index).get("filePath"), rs.getString("filePath"));
//					Assert.assertEquals(vouchers.get(index).get("expiryDate"), rs.getLong("expiryDate"));
				} while (rs.next());
				
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
			deleteUserVoucherByUserId(user.getId());
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}

		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
