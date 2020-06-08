package testCases.otp;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Get_Otp extends TestBase{
	
	private String id; 
	
	public TC_Get_Otp(String id) {
		this.id=id;
	}

	@Test
	void getOtpUser()
	{
		GetOtp(id);
	}
	
	@Test(dependsOnMethods = {"getOtpUser"})
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
		else if(code == 404)
		{
			Assert.assertEquals("OTP not found", message);
		}
	}
	
	@Test(dependsOnMethods = {"getOtpUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"getOtpUser"})
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