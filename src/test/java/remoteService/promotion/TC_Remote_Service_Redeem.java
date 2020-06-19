package remoteService.promotion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
			createUserVoucher(user.getId(), 1, 2); // cashback not used
			createUserVoucher(user.getId(), 7, 2); // discount not used
			createUserVoucher(user.getId(), 3, 1); // used
			createUserVoucher(user.getId(), 4, 1); // used
			createUserVoucher(user.getId(), 16, 2); // discount minpurchase 500K
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
		
		final String errorMessage1 = "your voucher not found";
		final String errorMessage2 = "user not found";
		final String errorMessage3 = "insufficient purchase amount to use this voucher";
		final String errorMessage4 = "your voucher is not applicable with your number";
		final String errorMessage5 = "unknown provider";
		final String errorMessage6 = "unknown payment method";
		final String errorMessage7 = "your voucher is not applicable with payment method";
		final String errorMessage8 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else if (responseBody.contains(errorMessage3)) {
			// do some code
		} else if (responseBody.contains(errorMessage4)) {
			// do some code
		} else if (responseBody.contains(errorMessage5)) {
			// do some code
		} else if (responseBody.contains(errorMessage6)) {
			// do some code
		} else if (responseBody.contains(errorMessage7)) {
			// do some code
		} else if (responseBody.contains(errorMessage8)) {
			// do some code
		} else {
			String voucherTypeName = response.getBody().jsonPath().getString("voucherTypeName");
			Assert.assertTrue(voucherTypeName.equals("cashback") || voucherTypeName.equals("discount"));
			
			if (voucherTypeName.equals("cashback")) 
				Assert.assertEquals(response.getBody().jsonPath().getString("finalPrice"), price);								
			else 
				Assert.assertEquals(response.getBody().jsonPath().getString("value"), "0");								
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";

		final String errorMessage1 = "your voucher not found";
		final String errorMessage2 = "user not found";
		final String errorMessage3 = "insufficient purchase amount to use this voucher";
		final String errorMessage4 = "your voucher is not applicable with your number";
		final String errorMessage5 = "unknown provider";
		final String errorMessage6 = "unknown payment method";
		final String errorMessage7 = "your voucher is not applicable with payment method";
		final String errorMessage8 = "invalid request format";

		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
					+ "WHERE A.userId = ? AND A.voucherId = ? AND A.voucherStatusId != 1 AND B.isActive = 1";
			param.put("1", Long.parseLong(userId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "promotion");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "member");			
			Assert.assertTrue(data.size() == 0);			
			break;			
		case errorMessage3:
			query = "SELECT * FROM voucher WHERE id = ?";
			param.put("1", Long.parseLong(voucherId));
			data = sqlExec(query, param, "promotion");
			
			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");			
			for (Map<String, Object> map : data)
				Assert.assertTrue(Long.parseLong(price) < (Long) map.get("minPurchase"));	
			
			break;
		case errorMessage4:
			query = "SELECT * FROM voucher A LEFT JOIN voucher_provider B on A.id = B.voucherId WHERE A.id = ? AND B.providerId = ?";
			param.put("1", Long.parseLong(voucherId));
			param.put("2", Long.parseLong(providerId));
			data = sqlExec(query, param, "promotion");
			Assert.assertTrue(data.size() == 0);
			break;			
		case errorMessage5:
			query = "SELECT * FROM provider id = ?";
			param.put("1", Long.parseLong(providerId));
			data = sqlExec(query, param, "order");
			Assert.assertTrue(data.size() == 0);
			break;			
		case errorMessage6:
			query = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ?";
			param.put("1", Long.parseLong(paymentMethodId));
			data = sqlExec(query, param, "promotion");			
			Assert.assertTrue(data.size() == 0);
			break;		
		case errorMessage7:
			query = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ? AND voucherId = ?";	
			param.put("1", Long.parseLong(paymentMethodId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "promotion");	
			Assert.assertTrue(data.size() == 0);
			break;		
		case errorMessage8:
			// do some code
			break;
		default:
			String voucherTypeName = response.getBody().jsonPath().getString("voucherTypeName");

			if (voucherTypeName.equals("cashback")) {
				query = "SELECT * FROM voucher WHERE id = ?";
				param.put("1", Long.parseLong(voucherId));
				data = sqlExec(query, param, "promotion");
				
				if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");				
				for (Map<String, Object> map : data)
					Assert.assertEquals(response.getBody().jsonPath().getString("value"), (String) map.get("value"));
				
			} else if (voucherTypeName.equals("discount")) {
				query = "SELECT * FROM voucher WHERE id = ?";
				param.put("1", Long.parseLong(voucherId));
				data = sqlExec(query, param, "promotion");
				
				if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");				
				for (Map<String, Object> map : data) {
					long discount = (Integer) map.get("discount") * Long.parseLong(price);
					
					if (discount > (Long) map.get("maxDeduction")) discount = (Long) map.get("maxDeduction");										
					Assert.assertEquals(response.getBody().jsonPath().getLong("finalPrice"), Long.parseLong(price) - discount);
				}
			}			
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteUserVoucherByUserId(user.getId());
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
