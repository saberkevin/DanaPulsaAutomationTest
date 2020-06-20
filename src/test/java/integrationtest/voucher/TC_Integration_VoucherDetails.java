package integrationtest.voucher;

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

public class TC_Integration_VoucherDetails extends TestBase {
	private User user = new User();
	private String testCase;
	private String voucherId;
	private String result;
	
	public TC_Integration_VoucherDetails(String testCase, String voucherId, String result) {
		this.testCase = testCase;
		this.voucherId = voucherId;
		this.result = result;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		// initialize user
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setUsername("081252930398");
		user.setPin(123456);
		
		// delete if exist
		deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
		deleteUserIfExist(user.getEmail(), user.getUsername());
		
		// register new user
		register(user.getName(), user.getEmail(), user.getUsername(), Integer.toString(user.getPin()));
		checkStatusCode("201");
		
		// login to system
		login("62" + user.getUsername().substring(1));
		checkStatusCode("200");
		user.setId(response.getBody().jsonPath().getLong("data.id"));
		
		// verify pin login
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		// get voucher id from promotion
		getPromotionVoucher(user.getSessionId(), "1");
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		List<Map<String, String>> vouchers = response.getBody().jsonPath().getList("data");
		voucherId = String.valueOf(vouchers.get(Integer.parseInt(voucherId) - 1).get("id"));
	}
	
	@Test
	public void testVoucherDetails() {		
		getVoucherDetails(user.getSessionId(), voucherId);
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
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
		}
	}
	
	@AfterClass
	public void afterClass() {
		// logout
		logout(user.getSessionId());
		checkStatusCode("200");
		
		// delete user
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
