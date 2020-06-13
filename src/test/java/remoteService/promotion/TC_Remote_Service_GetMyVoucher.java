package remoteService.promotion;

import java.util.List;

import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import model.User;
import model.Voucher;

public class TC_Remote_Service_GetMyVoucher extends TestBase {
	private User user = new User();
	private String userId;
	private String page;
	private Voucher[] vouchers;
	
	@SuppressWarnings("unchecked")
	public void getMyVoucherRemoteService(String userId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("page:" + page);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", "getMyVoucher");
		requestParams.put("request", "{\"userId\":" + userId + ",\"page\":" + page + "}");
		
		RestAssured.baseURI = URIPromotion;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, "/test");
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
		
		//hard code for testing
		userId = Long.toString(user.getId());
	}
	
	@Test
	public void testNyVouchers() {
		// call API promotion voucher remote service
//		getPromotionVoucherRemoteService(userId, page);
		getMyVoucherRemoteService(userId, "1");
	}
	
	@Test(dependsOnMethods = {"testNyVouchers"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			List<JSONObject> myVouchers = response.jsonPath().get();
			
			if(myVouchers != null) {
				vouchers = new Voucher[myVouchers.size()];
				
				for (int i = 0; i < myVouchers.size(); i++) {
					JSONObject voucher = myVouchers.get(i);
					
					vouchers[i].setId((long) voucher.get("id"));
				}
			}
		}
	}
	
	@Test
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			
		}
	}
	
	@AfterClass
	public void afterClass() {
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
