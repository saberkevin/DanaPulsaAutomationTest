package testCases.otp;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Forgot_Pin_Otp extends TestBase{
	
	private String id; 
	
	public TC_Forgot_Pin_Otp(String id) {
		this.id=id;
	}

	@Test
	void forgotPinOtpUser()
	{
		forgotPinOtp(id);
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
		}
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser"})
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