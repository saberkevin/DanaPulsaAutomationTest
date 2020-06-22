package remoteService.promotion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
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
	private String dataAMQP;

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
		String message = "{\"userId\":" + userId 
				+ ",\"voucherId\":" + voucherId 
				+ ",\"price\":" + price 
				+ ",\"providerId\":" + providerId 
				+ ",\"paymentMethodId\":" + paymentMethodId + "}";
		
		dataAMQP = callRP(promotionAMQP, ConfigRemoteServicePromotion.QUEUE_ELIGIBLE_TO_GET_VOUCHER, message);
		logger.info("message = " + message);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testIssue"})
	public void checkData() {
		Assert.assertTrue(dataAMQP.contains(result), dataAMQP);
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "user not found";
		final String errorMessage2 = "unknown payment method";
		final String errorMessage3 = "unknown provider id";
		final String errorMessage4 = "invalid request format";
		
		switch (dataAMQP) {
		case errorMessage1:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");			
			Assert.assertTrue(data.size() == 0);	
			break;
		case errorMessage2:
			query = "SELECT * FROM payment_method WHERE id = ?";
			param.put("1", Long.parseLong(paymentMethodId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);			
			break;			
		case errorMessage3:
			query = "SELECT * FROM provider WHERE id = ?";
			param.put("1", Long.parseLong(providerId));
			data = sqlExec(query, param, "ORDER");			
			Assert.assertTrue(data.size() == 0);	
			break;		
		case errorMessage4:
			// do some code
			break;
		default:
			// do some code
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
