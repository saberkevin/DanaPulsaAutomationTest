package remoteService.promotion;

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

public class TC_Remote_Service_EligibleToGetVoucher extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String price;
	private String providerId;
	private String voucherId;
	private String paymentMethodId;
	private String result;
	private boolean isCreateUser;

	public TC_Remote_Service_EligibleToGetVoucher(String testCase, String userId, String price, String providerId, String voucherId, String paymentMethodId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.price = price;
		this.providerId = providerId;
		this.voucherId = voucherId;
		this.paymentMethodId = paymentMethodId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void eligibleToGetVoucherRemoteService(String userId, String price, String voucherId, String providerId, String paymentMethodId) {
		logger.info("Call Payment API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("price:" + price);
		logger.info("provider id:" + providerId);
		logger.info("payment method id:" + paymentMethodId);
		logger.info("voucher id:" + voucherId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServicePromotion.QUEUE_ELIGIBLE_TO_GET_VOUCHER);
		requestParams.put("message", "{\"userId\":" + userId 
				+ ",\"voucherId\":" + voucherId 
				+ ",\"price\":" + price 
				+ ",\"providerId\":" + providerId 
				+ ",\"paymentMethodId\":" + paymentMethodId + "}");
		
		RestAssured.baseURI = ConfigRemoteServicePromotion.BASE_URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.GET, ConfigRemoteServicePromotion.ENDPOINT_PATH);
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
			
			// insert user into database
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());			
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));		
			createBalance(user.getId(), 10000000);
			userId = Long.toString(user.getId());
			
			// set flag
			isCreateUser = true;			
		}
	}
	
	@Test
	public void testIssue() {
		eligibleToGetVoucherRemoteService(userId, price, voucherId, providerId, paymentMethodId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testIssue"})
	public void checkData() {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);		
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		// do some code
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
