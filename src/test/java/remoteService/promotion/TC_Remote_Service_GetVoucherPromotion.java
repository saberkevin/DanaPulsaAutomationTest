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
	private String description;
	private String userId;
	private String page;
	private String result;
	
	public TC_Remote_Service_GetVoucherPromotion() {
		
	}
	
	public TC_Remote_Service_GetVoucherPromotion(String description, String userId, String page, String result) {
		this.description = description;
		this.userId = userId;
		this.page = page;
		this.result = result;
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
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + description);
	}
	
	@Test
	public void testPromotionVouchers() {
		getPromotionVoucherRemoteService(userId, page);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testPromotionVouchers"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.contains("Unexpected") && !responseBody.equals("There is no promotion right now")) {
			List<Map<String, String>> vouchers = response.jsonPath().get();
			
			for (int i = 0; i < vouchers.size(); i++) {
				Assert.assertNotNull(vouchers.get(i).get("id"));
				Assert.assertNotNull(vouchers.get(i).get("name"));
				Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
				Assert.assertNotNull(vouchers.get(i).get("filePath"));
				Assert.assertNotNull(vouchers.get(i).get("expiryDate"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("There is no promotion right now")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.id, "
						+ "A.name AS voucherName, "
						+ "B.name AS voucherTypeName, "
						+ "A.filePath, "
						+ "A.expiryDate "
						+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
						+ "WHERE A.isActive = 1 AND A.id NOT IN (SELECT voucherId FROM user_voucher where userId = ? AND voucherStatusId = 2) "
						+ "LIMIT ?, 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setInt(2, (Integer.parseInt(page)-1) * 10);
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.contains("Unexpected")) {
			// do some code
			
		} else {
			List<Map<String, String>> vouchers = response.jsonPath().get();
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.id, "
						+ "A.name AS voucherName, "
						+ "B.name AS voucherTypeName, "
						+ "A.filePath, "
						+ "A.expiryDate "
						+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
						+ "WHERE A.isActive = 1 AND A.id NOT IN (SELECT voucherId FROM user_voucher where userId = ? AND voucherStatusId = 2) "
						+ "LIMIT ?, 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setInt(2, (Integer.parseInt(page)-1) * 10);
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					int index = rs.getRow() - 1;
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("id")), rs.getString("id"));
					Assert.assertEquals(vouchers.get(index).get("name"), rs.getString("voucherName"));						
					Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), rs.getString("voucherTypeName"));						
					Assert.assertEquals(vouchers.get(index).get("filePath"), rs.getString("filePath"));						
//							Assert.assertEquals(vouchers.get(index).get("expiryDate"), rs.getLong("expiryDate"));
					
					System.out.print(String.valueOf(vouchers.get(index).get("expiryDate")) + " - ");
					System.out.println(rs.getLong("expiryDate"));
				}
				
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
