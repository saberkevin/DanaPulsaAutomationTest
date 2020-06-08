package testCases.otp;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Verify_Otp extends TestBase{
	
	private String id;
	private String code;
	
	public TC_Verify_Otp(String id, String code) {
		this.id=id;
		this.code=code;
	}

	@Test
	void verifyOtpUser()
	{
		verifyOtp(id, code);
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			Assert.assertNotEquals("", jsonPath.get("data.token"));
		}
		else if(code == 404)
		{
			Assert.assertEquals("incorrect OTP", message);
		}
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
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