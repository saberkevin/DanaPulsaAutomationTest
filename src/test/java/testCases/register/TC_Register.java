package testCases.register;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;

public class TC_Register extends TestBase{
	
	private String name;
	private String email; 
	private String phone; 
	private String pin; 
	
	public TC_Register(String name, String email, String phone, String pin) {
		this.name=name;
		this.email=email;
		this.phone=phone;
		this.pin=pin;
	}

	@Test
	void registerUser()
	{
		register(name,email,phone,pin);
	}
	
	@Test
	@Parameters("statusCode")
	void assertStatusCode(String sc)
	{
		checkStatusCode(sc);	
	}
	
	@Test
	@Parameters("responseTime")
	void assertResponseTime(String rt)
	{
		checkResponseTime(rt);
	}

	@Test
	void checkEmailValid()
	{
		JsonPath jsonPath = response.jsonPath();
		String checkEmail = jsonPath.get("email");
		logger.info(checkEmail);
		
		String regex = "^(.+)@(.+).(.+)$";
		 
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(checkEmail);
		
		boolean isValid = matcher.matches();
		
		Assert.assertTrue(isValid);	
	}
	
	@Test
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