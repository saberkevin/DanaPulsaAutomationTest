package testCases.login;

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
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id")));
			Assert.assertNotEquals("", jsonPath.get("data.name"));
			Assert.assertNotEquals("", jsonPath.get("data.email"));
			Assert.assertEquals(replacePhoneForAssertion(phone), jsonPath.get("data.username"));
			checkEmailValid(jsonPath.get("data.email"));
			checkResultPhoneValid(jsonPath.get("data.username"));
			
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
	
	@AfterClass
	void end()
	{
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}