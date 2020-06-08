package testCases.order;

import java.util.List;
import java.util.Map;

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
	
	@Test
	public void testMobileRechargeCatalog() {
		getCatalog(user, phoneNumber);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		if(code.contentEquals("200")) {
			String message = response.getBody().jsonPath().getString("message");
			Assert.assertEquals(message, "success");

			if(!response.getBody().jsonPath().get("data").equals("[]")) {
				List<Map<String, String>> data = response.getBody().jsonPath().getList("data");				

				for (int i = 0; i < data.size(); i++) {
					Provider provider = new Provider();
					provider.setId(data.get(i).get("provider.id"));
					provider.setName(data.get(i).get("provider.name"));
					provider.setImage(data.get(i).get("provider.image"));	
					
					Assert.assertTrue(isProviderTrue(phoneNumber, provider));
					
					Catalog catalog = new Catalog();
					catalog.setId(data.get(i).get("catalog.id"));
					catalog.setValue(Integer.parseInt(data.get(i).get("catalog.value")));
					catalog.setPrice(Integer.parseInt(data.get(i).get("catalog.price")));
					
					Assert.assertNotNull(catalog.getId());
					Assert.assertNotNull(catalog.getValue());
					Assert.assertNotNull(catalog.getPrice());
				}
			}		
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
