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

public class TC_Remote_Service_Issue extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String price;
	private String providerId;
	private String voucherId;
	private String paymentMethodId;
	private boolean isCreateUser;

	public TC_Remote_Service_Issue(String testCase, String userId, String price, String providerId, String voucherId, String paymentMethodId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.price = price;
		this.providerId = providerId;
		this.voucherId = voucherId;
		this.paymentMethodId = paymentMethodId;
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
		String message =  "{\"userId\":" + userId 
				+ ",\"price\":" + price 
				+ ",\"providerId\":" + providerId 
				+ ",\"voucherId\":" + voucherId 
				+ ",\"paymentMethodId\":" + paymentMethodId + "}";
		
		persistentCall(promotionAMQP, ConfigRemoteServicePromotion.QUEUE_ISSUE_VOUCHER, message);
	}
	
	@Test(dependsOnMethods = {"testIssue"})
	public void checkData() {
		logger.info("checking data");
		// do some code
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		logger.info("checking database");
		
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";

		query = "SELECT * FROM user_voucher WHERE userId = ? AND voucherId = ?";
		param.put("1", Long.parseLong(userId));
		param.put("2", Long.parseLong(voucherId));
		data = sqlExec(query, param, "PROMOTION");

		if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
		for (Map<String, Object> map : data) {
			Assert.assertEquals(map.get("voucherId"), voucherId);
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
