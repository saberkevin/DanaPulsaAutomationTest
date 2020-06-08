package testCases.payment;

import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.Transaction;
import model.User;

public class TC_Pay_Order extends TestBase {
	private User user;
	private Transaction transaction;
	private String paymentMethodId;

	public TC_Pay_Order(String sessionId, String transactionId, String paymentMethodId, String voucherId) {
		user.setSessionId(sessionId);
		transaction.setId(transactionId);
		this.paymentMethodId = paymentMethodId;
		transaction.getVoucher().setId(voucherId);
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
		getCatalog(user, user.getPhoneNumber());
		checkStatusCode("200");
		
		createOrder(user, user.getPhoneNumber(), transaction.getCatalog());
		checkStatusCode("201");		
	}
	
	@Test
	public void testPayOrder() {
		payOrder(user, transaction, paymentMethodId);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		
		if(code.equals("400")) {
			Assert.assertTrue(
					message.equals("not enough balance") || 
					message.equals("unknown method") || 
					message.equals("unknown voucher")
					);
		} else if(code.equals("404")) {
			Assert.assertTrue(
					message.equals("selected catalog is not available for this phone’s provider") || 
					message.equals("you don’t have any vouchers recommendation")
					);
		} else if (code.equals("202")) {
			Assert.assertEquals(message, "success");
			
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");
			Assert.assertEquals(data.get("id"), transaction.getId());
			Assert.assertEquals(data.get("phone"), user.getPhoneNumber());
			Assert.assertEquals(data.get("catalog.id"), transaction.getCatalog().getId());
			Assert.assertEquals(data.get("catalog.provider"), transaction.getCatalog().getProvider());
			Assert.assertEquals(data.get("catalog.value"), transaction.getCatalog().getValue());
			Assert.assertEquals(data.get("catalog.price"), transaction.getCatalog().getPrice());
			Assert.assertEquals(data.get("voucher.id"), transaction.getVoucher().getId());
			Assert.assertEquals(data.get("voucher.name"), transaction.getVoucher().getName());
			Assert.assertEquals(data.get("voucher.deduction"), transaction.getVoucher().getDiscount());
			Assert.assertEquals(data.get("voucher.maxDeduction"), transaction.getVoucher().getMaximumDeduction());
			Assert.assertEquals(data.get("method"), transaction.getPaymentMethod());
			Assert.assertEquals(data.get("status"), transaction.getStatus());
			Assert.assertEquals(data.get("createdAt"), transaction.getCreatedAt());
			Assert.assertEquals(data.get("updatedAt"), transaction.getUpdatedAt());
		}
	}

	@AfterMethod
	public void afterMethod() {
		
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());				
	}
}
