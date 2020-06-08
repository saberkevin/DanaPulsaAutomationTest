package testCases.order;

import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.Catalog;
import model.User;

public class TC_Create_Order extends TestBase {
	private User user;
	private String phoneNumber;
	private Catalog catalog;
	
	public TC_Create_Order(String sessionId, String phoneNumber, String catalogId) {
		user.setSessionId(sessionId);
		this.phoneNumber = phoneNumber;
		catalog.setId(catalogId);
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
		getCatalog(user, phoneNumber);
		checkStatusCode("200");
	}
	
	@Test
	public void testCreateOrder() {
		createOrder(user, phoneNumber, catalog);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		if(code.equals("200")) {
			String message = response.getBody().jsonPath().getString("message");
			Assert.assertEquals(message, "success");
			
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");
			Assert.assertNotNull(data.get("id"));
			Assert.assertEquals(data.get("phone"), phoneNumber);
			Assert.assertEquals(data.get("catalog.id"), catalog.getId());
			Assert.assertEquals(data.get("catalog.provider"), catalog.getProvider());
			Assert.assertEquals(data.get("catalog.value"), catalog.getValue());
			Assert.assertEquals(data.get("catalog.price"), catalog.getPrice());
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
