package testCases.voucher;

import java.text.SimpleDateFormat;
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

public class TC_Voucher_Details extends TestBase {
	private User user = new User();
	private String testCase;
	private String sessionId;
	private String voucherId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Voucher_Details(String testCase, String sessionId, String voucherId, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.voucherId = voucherId;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (sessionId.equals("true")) {
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
			
			verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
			checkStatusCode("200");
			user.setSessionId(response.getCookie("JSESSIONID"));
			sessionId = user.getSessionId();
			
			// set flag
			isCreateUser = true;			
		}
	}
	
	@Test
	public void testVoucherDetails() {		
		getVoucherDetails(sessionId, voucherId);
		user.setSessionId(response.getCookie("JSESSIONID"));

		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();
		if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertTrue(response.getBody().asString().contains("Not Found") 
					|| response.getBody().asString().contains("voucher not found"));
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
		}
	}
	
	@Test(dependsOnMethods = {"testVoucherDetails"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			Map<String, String> data = response.getBody().jsonPath().get("data");
			
			Assert.assertEquals(String.valueOf(data.get("id")), voucherId);
			Assert.assertNotNull(data.get("name"));
			Assert.assertNotNull(data.get("voucherTypeName"));
			Assert.assertNotNull(data.get("discount"));
			Assert.assertNotNull(data.get("minPurchase"));
			Assert.assertNotNull(data.get("maxDeduction"));
			Assert.assertNotNull(data.get("value"));
			Assert.assertNotNull(data.get("filePath"));
			Assert.assertNotNull(data.get("expiryDate"));
			Assert.assertNotNull(data.get("active"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		int statusCode = response.getStatusCode();		
		if (statusCode == 200) {
			query = "SELECT A.*, B.name AS voucherTypeName FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id WHERE A.id = ?";
			param.put("1", voucherId);
			data = sqlExec(query, param, "PROMOTION");
			
			Map<String, Object> voucher = response.getBody().jsonPath().get("data");

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(voucher.get("id"), map.get("id"));
				Assert.assertEquals(voucher.get("name"), map.get("name"));
				Assert.assertEquals(voucher.get("discount"), map.get("discount"));
				Assert.assertEquals(voucher.get("voucherTypeName"), map.get("voucherTypeName"));
				Assert.assertEquals(Long.valueOf((Integer) voucher.get("minPurchase")), map.get("minPurchase"));
				Assert.assertEquals(Long.valueOf((Integer) voucher.get("maxDeduction")), map.get("maxDeduction"));
				Assert.assertEquals(voucher.get("value"), map.get("value"));
				Assert.assertEquals(voucher.get("filePath"), map.get("filePath"));

				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				Assert.assertEquals(formatter.format(voucher.get("expiryDate")), formatter.format(map.get("expiryDate")));
				Assert.assertEquals(voucher.get("active"), map.get("isActive"));
			}
		} else if (statusCode == 404) {
			if (response.getBody().asString().contains("voucher not found")) {
				query = "SELECT * FROM voucher WHERE id = ?";
				param.put("1", Long.parseLong(voucherId));
				data = sqlExec(query, param, "PROMOTION");
				Assert.assertTrue(data.size() == 0);
			}
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
