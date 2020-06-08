package testCases.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Login extends TestBase{
	
	private String phone; 
	
	public TC_Login(String phone) {
		this.phone=phone;
	}

	@Test
	void loginUser()
	{
		login(phone);
	}
	
	@Test(dependsOnMethods = {"loginUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			//id
			//Assert.assertEquals(name, jsonPath.get("data.name"));
			//Assert.assertEquals(email, jsonPath.get("data.email"));
			Assert.assertEquals(phone, jsonPath.get("data.username"));
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid") || message.contains("incorrect"));
		}
	}
	
	@Test(dependsOnMethods = {"loginUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"loginUser"})
	@Parameters("responseTime")
	void assertResponseTime(String rt)
	{
		checkResponseTime(rt);
	}

	void checkEmailValid()
	{
		JsonPath jsonPath = response.jsonPath();
		//String code = jsonPath.get("code");
		String checkEmail = jsonPath.get("email");
		logger.info(checkEmail);
		
		String regex = "^(.+)@(.+).(.+)$";
		 
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(checkEmail);
		
		boolean isValid = matcher.matches();
		
		Assert.assertTrue(isValid);	
	}

	void checkPhoneNumber()
	{
		JsonPath jsonPath = response.jsonPath();
		String checkPhone = jsonPath.get("phone");
		logger.info(checkPhone);
		
		String regex = "^08[0-9]{9,13}$";
		 
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(checkPhone);
		
		boolean isValid = matcher.matches();
		
		Assert.assertTrue(isValid);	
	}
	
	@AfterClass
	void end()
	{
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}