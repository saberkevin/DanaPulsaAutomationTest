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
import model.Transaction;
import model.User;

public class TC_Remote_Service_GetVoucherRecommendation extends TestBase {
	private User user = new User();
	private Transaction transaction = new Transaction();
	private String userId;
	private String transactionId;
	
	public TC_Remote_Service_GetVoucherRecommendation(String userId, String transactionId) {
		this.userId = userId;
		this.transactionId = transactionId;
	}
	
	@SuppressWarnings("unchecked")
	public void getVoucherRecommendationRemoteService(String userId, String transactionId) {
		logger.info("Call Get Voucher Recommendation API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("transactionId:" + transactionId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", "getMyVoucher");
		requestParams.put("request", "{\"userId\":" + userId + ",\"transactionId\":" + transactionId + "}");
		
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
		
		// insert transaction into database - TELKOMSEL 30k
		transaction.setCatalogId(16);
		deleteTransactionByUserIdIfExist(user.getId());
		createTransaction(user.getId(), user.getUsername(), transaction.getCatalogId());
		transaction.setId(getTransactionIdByUserId(user.getId()));

		// if data from excel "true", then get valid transaction id
		if (transactionId.equals("true")) {
			transactionId = Long.toString(transaction.getId());
		}
	}
	
	@Test
	public void testVoucherRecommendation() {
		// call API voucher recommendation remote service
		getVoucherRecommendationRemoteService(userId, transactionId);
	}
	
	@Test(dependsOnMethods = {"testVoucherRecommendation"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			List<Map<String, String>> vouchers = response.jsonPath().get();
			
			if(vouchers != null) {
				for (int i = 0; i < vouchers.size(); i++) {
					Assert.assertNotNull(vouchers.get(i).get("id"));
					Assert.assertNotNull(vouchers.get(i).get("name"));
					Assert.assertNotNull(vouchers.get(i).get("discount"));
					Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
					Assert.assertNotNull(vouchers.get(i).get("maxDeduction"));
					Assert.assertNotNull(vouchers.get(i).get("filePath"));
					Assert.assertNotNull(vouchers.get(i).get("expiryDate"));
				}
			}
		}  else if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "invalid user id");
		} else if (statusCode == 404) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "404");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "invalid transaction id");
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			long price = 0;
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT catalogId, price "
						+ "FROM transaction LEFT JOIN pulsa_catalog on A.catalogId = B.id "
						+ "WHERE userId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					Assert.assertEquals(rs.getLong("id"), transaction.getId());
					
					Assert.assertNotNull(rs.getLong("catalogId"));
					transaction.setCatalogId(rs.getLong("catalogId"));
					
					Assert.assertNotNull(rs.getLong("price"));
					price = rs.getLong("price");
				}
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			List<Map<String, String>> vouchers = response.jsonPath().get();
			
			if(vouchers != null) {
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT "
							+ "B.id, "
							+ "B.name AS voucherName, "
							+ "B.discount "
							+ "C.name AS voucherTypeName, "
							+ "B.maxDeduction "
							+ "B.filePath, "
							+ "B.expiryDate "
							+ "FROM user_voucher A LEFT JOIN voucher B on A.voucherId = B.id "
							+ "LEFT JOIN voucher_type C on B.typeId = C.id "
							+ "WHERE B.minPurchase < ? ORDER BY B.id";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, price);
					
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
