package testCases.order;

import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.Catalog;
import model.Provider;
import model.User;

public class TC_Mobile_Recharge_Catalog extends TestBase {
	private User user;
	private String phoneNumber;
	
	public TC_Mobile_Recharge_Catalog(String sessionId, String phoneNumber) {
		user.setSessionId(sessionId);
		this.phoneNumber = phoneNumber;
	}
	
	private boolean isProviderTrue(String phoneNumber, Provider provider) {
		return false;
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
	public void testMobileRechargeCatalog() {
		getCatalog(user, phoneNumber);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		if(code.equals("200")) {
			String message = response.getBody().jsonPath().getString("message");
			Assert.assertEquals(message, "success");
			
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");

			Provider provider = new Provider();
			provider = (Provider) data.get("provider");					
			Assert.assertTrue(isProviderTrue(phoneNumber, provider));
			
			JSONArray catalogs = (JSONArray) data.get("catalog");
			Iterator<Catalog> itr = catalogs.iterator();
			while(itr.hasNext()) {
				Catalog catalog = (Catalog) itr.next();
				Assert.assertNotNull(catalog.getId());
				Assert.assertNotNull(catalog.getValue());
				Assert.assertNotNull(catalog.getPrice());
			}		
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
