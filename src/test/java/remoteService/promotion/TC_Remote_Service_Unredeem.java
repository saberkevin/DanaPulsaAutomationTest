package remoteService.promotion;

import java.text.ParseException;
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

public class TC_Remote_Service_Unredeem extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String voucherId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_Unredeem(String testCase, String userId, String voucherId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.voucherId = voucherId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void getUnredeemRemoteService(String userId, String voucherId) {
		logger.info("Call Unredeem API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("voucher id:" + voucherId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", ConfigRemoteServicePromotion.QUEUE_UNREDEEM);
		requestParams.put("request", "{\"userId\":" + userId + ",\"voucherId\":" + voucherId + "}");
		
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
			createUserVoucher(user.getId(), 1, 1); // voucher used
			
			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testUnredeem() {
		getUnredeemRemoteService(userId, voucherId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testUnredeem"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		final String errorMessage1 = "user not found";
		final String errorMessage2 = "voucher not found";
		final String errorMessage3 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else if (responseBody.contains(errorMessage3)) {
			// do some code
		} else {
			// do some code
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "user not found";
		final String errorMessage2 = "voucher not found";
		final String errorMessage3 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
					+ "WHERE A.userId = ? AND A.voucherId = ? AND B.isActive = 1";
			param.put("1", Long.parseLong(userId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;			
		case errorMessage3:
			// do some code
			break;
		default:
			query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
					+ "WHERE A.userId = ? AND A.voucherId = ? AND B.isActive = 1";
			param.put("1", Long.parseLong(userId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(map.get("voucherStatusId"), 2);
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
