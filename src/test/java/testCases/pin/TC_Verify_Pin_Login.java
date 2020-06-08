package testCases.pin;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Verify_Pin_Login extends TestBase{
	
	private String id;
	private String pin; 
	
	public TC_Verify_Pin_Login(String id, String pin) {
		this.id=id;
		this.pin=pin;
	}

	@Test
	void verifyPinLoginUser()
	{
		verifyPinLogin(id, pin);
	}
	
	@Test(dependsOnMethods = {"verifyPinLoginUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertNotEquals("", jsonPath.get("data.token"));
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid") || message.contains("incorrect"));
		}
	}
	
	@Test(dependsOnMethods = {"verifyPinLoginUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"verifyPinLoginUser"})
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