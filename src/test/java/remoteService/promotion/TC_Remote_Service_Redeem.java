package remoteService.promotion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import model.User;

public class TC_Remote_Service_Redeem extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String voucherId;
	private String price;
	private String paymentMethodId;
	private String providerId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_Redeem() {
		
	}
	
	public TC_Remote_Service_Redeem(String testCase, String userId, String voucherId, String price, String paymentMethodId, String providerId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.voucherId = voucherId;
		this.price = price;
		this.paymentMethodId = paymentMethodId;
		this.providerId = providerId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void redeemRemoteService(String userId, String voucherId, String price, String paymentMethodId, String providerId) {
		logger.info("Call Redeem API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("voucherId:" + voucherId);
		logger.info("price:" + price);
		logger.info("paymentMethodId:" + paymentMethodId);
		logger.info("providerId:" + providerId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", "redeem");
		requestParams.put("request", "{\"userId\":" + userId + ",\"voucherId\":" + voucherId 
				+ ",\"price\":" + price + ",\"paymentMethodId\":" + paymentMethodId + ",\"providerId\":" + providerId + "}");
		
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
			createUserVoucher(user.getId(), 1, 2);
			createUserVoucher(user.getId(), 7, 2);
			createUserVoucher(user.getId(), 3, 1);
			createUserVoucher(user.getId(), 4, 1);
		}
	}
	
	@Test
	public void testRedeem() {
		redeemRemoteService(userId, voucherId, price, paymentMethodId, providerId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testRedeem"})
	public void checkData() {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.contains("invalid request format") 
				&& !responseBody.equals("user not found")
				&& !responseBody.equals("unknown payment method")
				&& !responseBody.equals("your voucher not found")
				&& !responseBody.equals("your voucher is not applicable with your number")
				&& !responseBody.equals("your voucher is not applicable with payment method")) {

			Assert.assertTrue(response.getBody().jsonPath().getString("voucherTypeName").equals("cashback")
					|| response.getBody().jsonPath().getString("voucherTypeName").equals("discount"));
			
			if (response.getBody().jsonPath().getString("voucherTypeName").equals("cashback")) {
				Assert.assertEquals(response.getBody().jsonPath().getString("finalPrice"), price);								
			} else if (response.getBody().jsonPath().getString("voucherTypeName").equals("discount")) {
				Assert.assertEquals(response.getBody().jsonPath().getString("value"), "0");								
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("your voucher not found")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT * FROM user_voucher A "
						+ "LEFT JOIN voucher B ON A.voucherId = B.id "
						+ "WHERE A.id = ? AND A.voucherId = ? AND A.voucherStatusId != 1 AND B.isActive = 1";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(userId));
				ps.setLong(2, Long.parseLong(voucherId));
				
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
			
		} else if (responseBody.contains("your voucher is not applicable with your number")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT * FROM voucher A "
						+ "LEFT JOIN voucher_provider B on A.id = B.voucherId "
						+ "WHERE A.id = ? AND B.providerId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(voucherId));
				ps.setLong(2, Long.parseLong(providerId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.contains("unknown provider")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT * FROM voucher A "
						+ "LEFT JOIN voucher_provider B on A.id = B.voucherId "
						+ "WHERE A.id = ? AND B.providerId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(voucherId));
				ps.setLong(2, Long.parseLong(providerId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.contains("unknown payment method")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(paymentMethodId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		} else if (responseBody.contains("your voucher is not applicable with payment method")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(paymentMethodId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		} else {
			if (response.getBody().jsonPath().getString("voucherTypeName").equals("cashback")) {
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT * FROM voucher WHERE id = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1,Long.parseLong(voucherId));
					
					ResultSet rs = ps.executeQuery();
					
					if (!rs.next()) {
						Assert.assertTrue(false, "no voucher found in database");
					}
					do {
						Assert.assertEquals(response.getBody().jsonPath().getLong("value"), rs.getLong("value"));
					} while (rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (response.getBody().jsonPath().getString("voucherTypeName").equals("discount")) {
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT * FROM voucher WHERE id = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1,Long.parseLong(voucherId));
					
					ResultSet rs = ps.executeQuery();
					
					if (!rs.next()) {
						Assert.assertTrue(false, "no voucher found in database");
					}
					do {
						long discount = rs.getLong("discount") * Long.parseLong(price);

						if (discount > rs.getLong("maxDeduction")) {
							discount = rs.getLong("maxDeduction");
						}
						
						Assert.assertEquals(response.getBody().jsonPath().getLong("finalPrice"), Long.parseLong(price) - discount);
					} while (rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
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
