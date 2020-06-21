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

public class TC_Remote_Service_Unredeem extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String voucherId;
	private boolean isCreateUser;
	
	public TC_Remote_Service_Unredeem(String testCase, String userId, String voucherId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.voucherId = voucherId;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);

		if (userId.equals("true"))  {
			// initialize user
			user.setName(ConfigRemoteServicePromotion.USER_NAME);
			user.setEmail(ConfigRemoteServicePromotion.USER_EMAIL);
			user.setUsername(ConfigRemoteServicePromotion.USER_USERNAME);
			user.setPin(ConfigRemoteServicePromotion.USER_PIN);
			
			// insert user into database and get user id from it
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());			
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));		
			createBalance(user.getId(), 10000000);
			userId = Long.toString(user.getId());
			
			// insert voucher for user
			createUserVoucher(user.getId(), Long.parseLong(voucherId), 1); // voucher used
			
			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testUnredeem() {
		String message =  "{\"userId\":" + userId + ",\"voucherId\":" + voucherId + "}";		
		persistentCall(promotionAMQP, ConfigRemoteServicePromotion.QUEUE_UNREDEEM, message);
	}
	
	@Test(dependsOnMethods = {"testUnredeem"})
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
		
		query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
				+ "WHERE A.userId = ? AND A.voucherId = ? AND B.isActive = 1";
		param.put("1", Long.parseLong(userId));
		param.put("2", Long.parseLong(voucherId));
		data = sqlExec(query, param, "PROMOTION");

		if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
		for (Map<String, Object> map : data) {
			Assert.assertEquals(map.get("voucherStatusId"), 2);
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
