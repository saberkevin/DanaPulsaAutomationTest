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
import remoteService.order.ConfigRemoteServiceOrder;

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
		requestParams.put("queue", ConfigRemoteServicePromotion.QUEUE_REDEEM);
		requestParams.put("request", "{\"userId\":" + userId + ",\"voucherId\":" + voucherId 
				+ ",\"price\":" + price + ",\"paymentMethodId\":" + paymentMethodId + ",\"providerId\":" + providerId + "}");
		
		RestAssured.baseURI = ConfigRemoteServicePromotion.BASE_URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, ConfigRemoteServicePromotion.ENDPOINT_PATH);
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (userId.equals("true")) {
			// initialize user
			user.setName(ConfigRemoteServiceOrder.USER_NAME);
			user.setEmail(ConfigRemoteServiceOrder.USER_EMAIL);
			user.setUsername(ConfigRemoteServiceOrder.USER_USERNAME);
			user.setPin(ConfigRemoteServiceOrder.USER_PIN);
			
			// delete if exist
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());
			
			// register new user
			register(user.getName(), user.getEmail(), user.getUsername(), Integer.toString(user.getPin()));
			checkStatusCode("201");
			user.setId(response.getBody().jsonPath().getLong("data.id"));
			user.setBalance(15000000);
			userId = Long.toString(user.getId());
			
			// insert voucher into database	
//			if (voucherId.equals("1") || voucherId.equals("7") || voucherId.equals("16")) {				
//				createUserVoucher(user.getId(), Long.parseLong(voucherId), 2);
//				logger.info("create voucher id " + voucherId);
//			} else if (voucherId.equals("3") || voucherId.equals("4")) {
//				createUserVoucher(user.getId(), Long.parseLong(voucherId), 1);
//			}
			
			// set flag
			isCreateUser = true;			
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
		final String errorMessage8 = "voucher you want to redeem is either not found, has been used or already expired";
		final String errorMessage9 = "invalid request format";
		
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
		} else if (responseBody.contains(errorMessage9)) {
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
		final String errorMessage8 = "voucher you want to redeem is either not found, has been used or already expired";
		final String errorMessage9 = "invalid request format";

		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
					+ "WHERE A.userId = ? AND A.voucherId = ? AND A.voucherStatusId != 1 AND B.isActive = 1";
			param.put("1", Long.parseLong(userId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");			
			Assert.assertTrue(data.size() == 0);			
			break;			
		case errorMessage3:
			query = "SELECT * FROM voucher WHERE id = ?";
			param.put("1", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			
			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");			
			for (Map<String, Object> map : data)
				Assert.assertTrue(Long.parseLong(price) < (Long) map.get("minPurchase"));	
			
			break;
		case errorMessage4:
			query = "SELECT * FROM voucher A LEFT JOIN voucher_provider B on A.id = B.voucherId WHERE A.id = ? AND B.providerId = ?";
			param.put("1", Long.parseLong(voucherId));
			param.put("2", Long.parseLong(providerId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;			
		case errorMessage5:
			query = "SELECT * FROM provider WHERE id = ?";
			param.put("1", Long.parseLong(providerId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;			
		case errorMessage6:
			query = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ?";
			param.put("1", Long.parseLong(paymentMethodId));
			data = sqlExec(query, param, "PROMOTION");			
			Assert.assertTrue(data.size() == 0);
			break;		
		case errorMessage7:
			query = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ? AND voucherId = ?";	
			param.put("1", Long.parseLong(paymentMethodId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");	
			Assert.assertTrue(data.size() == 0);
			break;		
		case errorMessage8:
			query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
					+ "WHERE A.userId = ? AND A.voucherId = ? AND A.voucherStatusId != 1 AND B.isActive = 1";
			param.put("1", Long.parseLong(userId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;	
		case errorMessage9:
			// do some code
			break;
		default:
			String voucherTypeName = response.getBody().jsonPath().getString("voucherTypeName");

			if (voucherTypeName.equals("cashback")) {
				query = "SELECT * FROM voucher WHERE id = ?";
				param.put("1", Long.parseLong(voucherId));
				data = sqlExec(query, param, "PROMOTION");
				
				if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");				
				for (Map<String, Object> map : data)
					Assert.assertEquals(response.getBody().jsonPath().getInt("value"), map.get("value"));
				
			} else if (voucherTypeName.equals("discount")) {
				query = "SELECT * FROM voucher WHERE id = ?";
				param.put("1", Long.parseLong(voucherId));
				data = sqlExec(query, param, "PROMOTION");
				
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
//			deleteUserVoucherByUserId(user.getId());
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
