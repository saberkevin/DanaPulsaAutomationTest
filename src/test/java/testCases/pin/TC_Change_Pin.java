package testCases.pin;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Change_Pin extends TestBase{
	
	private String pin; 
	
	public TC_Change_Pin(String pin) {
		this.pin=pin;
	}

	@Test
	void changePinUser()
	{
		changePin(pin);
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("updated", message);
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid"));
		}
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
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