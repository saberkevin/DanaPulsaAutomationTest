package testCases.voucher;

import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.User;
import model.Voucher;

public class TC_My_Vouchers extends TestBase {
	private User user;
	private String page;
	
	public TC_My_Vouchers(String sessionId, String page) {
		user.setSessionId(sessionId);
		this.page = page;
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

	@SuppressWarnings("unchecked")
	@Test
	public void testMyVouchers() {
		getMyVoucher(user, page);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		
		if(code.equals("404")) {
			Assert.assertEquals(message, "you don’t have any vouchers");
		} else if (code.equals("200")) {
			Assert.assertEquals(message, "success");
			
			JSONArray vouchers = (JSONArray) response.getBody().jsonPath().getList("data");
			Iterator<Voucher> itr = vouchers.iterator();
			while(itr.hasNext()) {
				Voucher voucher = (Voucher) itr.next();
				Assert.assertNotNull(voucher.getId());
				Assert.assertNotNull(voucher.getName());
				Assert.assertNotNull(voucher.getVoucherTypeName());
				Assert.assertNotNull(voucher.getDiscount());
				Assert.assertNotNull(voucher.getMaximumDeduction());
				Assert.assertNotNull(voucher.getFilePath());
				Assert.assertNotNull(voucher.getExpiredDate());
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
