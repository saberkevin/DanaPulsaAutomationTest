package testCases.otp;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Change_Pin_Otp extends TestBase{

	@Test
	void changePinOtpUser()
	{
		changePinOtp();
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			Assert.assertNotEquals("", jsonPath.get("data.id"));
			Assert.assertNotEquals("", jsonPath.get("data.userId"));
			Assert.assertNotEquals("", jsonPath.get("data.code"));
		}
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
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