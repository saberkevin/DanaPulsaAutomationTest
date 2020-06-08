package testCases.voucher;

import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.User;
import model.Voucher;

public class TC_Voucher_Details extends TestBase {
	private User user;
	private Voucher voucher;
	
	public TC_Voucher_Details(String sessionId, String voucherId) {
		user.setSessionId(sessionId);
		voucher.setId(voucherId);
	}
	
	@BeforeClass
	public void beforeClass() {
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setPhoneNumber("081252930398");
		user.setPin("123456");

		register(user.getName(), user.getEmail(), user.getPhoneNumber(), user.getPin());
		checkStatusCode("200");

		login(user.getPhoneNumber());
		checkStatusCode("200");
		Map<String, String> data = response.getBody().jsonPath().getMap("data");
		user.setId(data.get("id"));
		
		verifyPinLogin(user.getId(), user.getPin());
		checkStatusCode("200");
	}
	
	@BeforeMethod
	public void berforeMethod() {
		getPromotionVoucher(user, "1");
		checkStatusCode("200");
	}
	
	@Test
	public void testVoucherDetails() {
		getVoucherDetails(user, voucher);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		Assert.assertTrue(message.equals("success") || message.equals("voucher not found"));
		
		JSONObject data = response.getBody().jsonPath().getJsonObject("data");
		Assert.assertEquals(data.get("id"), voucher.getId());
		Assert.assertNotNull(data.get("name"));
		Assert.assertNotNull(data.get("filePath"));
		Assert.assertNotNull(data.get("voucherTypeName"));
		Assert.assertNotNull(data.get("paymentMethodName"));
		Assert.assertNotNull(data.get("discount"));
		Assert.assertNotNull(data.get("maxDeduction"));
		Assert.assertNotNull(data.get("minPurchase"));
		Assert.assertNotNull(data.get("expiryDate"));
		Assert.assertNotNull(data.get("term"));
		Assert.assertNotNull(data.get("condition"));
		Assert.assertNotNull(data.get("instruction"));
	}

	@AfterMethod
	public void afterMethod() {
		
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
