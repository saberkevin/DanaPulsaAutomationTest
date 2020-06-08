package testCases.order;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.Provider;
import model.User;

public class TC_Recent_Phone_Number extends TestBase {
	private User user;
	
	public TC_Recent_Phone_Number(String sessionId) {
		user.setSessionId(sessionId);
	}
	
	private boolean isPhoneNumberRegexTrue(String phoneNumber) {
		String regex = "^(?=.[0-9])(?=.[a-z])(?=.[A-Z])(?=.[!@#$%^&*])(?=\\\\S+$).{8,}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(phoneNumber);
		return matcher.matches();
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
	public void testRecentPhoneNumber() throws ParseException {
		getRecentPhoneNumber(user);

		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		if(code.contentEquals("200")) {
			String message = response.getBody().jsonPath().getString("message");
			Assert.assertEquals(message, "success");

			if(!response.getBody().jsonPath().get("data").equals("[]")) {
				List<Map<String, String>> data = response.getBody().jsonPath().getList("data");				

				for (int i = 0; i < data.size(); i++) {
					String phoneNumber = data.get(i).get("number");
					Assert.assertTrue(isPhoneNumberRegexTrue(phoneNumber));

					Provider provider = new Provider();
					provider.setId(data.get(i).get("provider.id"));
					provider.setName(data.get(i).get("provider.name"));
					provider.setImage(data.get(i).get("provider.image"));	
					
					Assert.assertTrue(isProviderTrue(phoneNumber, provider));
					
					String dateString = data.get(i).get("date");
					DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
					Date date = format.parse(dateString);
				}
			}		
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
