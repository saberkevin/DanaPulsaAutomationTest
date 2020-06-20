package testCases.otp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Forgot_Pin_Otp extends TestBase{
	
	private String id; 
	private String codeBefore = "";
	
	public TC_Forgot_Pin_Otp(String id) {
		this.id=id;
	}

	@Test
	void forgotPinOtpUser()
	{
		logger.info("***** GET OTP Before *****");
		GetOtp(id);
		if(response.getStatusCode() == 200)
		{
			codeBefore = response.jsonPath().get("data.code");
		}
		logger.info("***** END GET OTP Before *****");
		forgotPinOtp(id);
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser","assertStatusCode","assertResponseTime"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(id));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(id), result.get("userId"));
				Assert.assertNotEquals(codeBefore, result.get("code"));
			}
			
			logger.info("***** Assert GET OTP After *****");
			GetOtp(id);
			Assert.assertNotEquals(codeBefore, response.jsonPath().get("data.code"));
			logger.info("***** END Assert GET OTP After *****");
		}
		else if(code == 404)
		{
			Assert.assertEquals("user not found", message);
		}
		else if(code == 500)
		{
			Assert.assertEquals("unverified number", message);
		}
		else if(code == 400)
		{
			Assert.assertEquals("invalid request format", message);
		}
		else
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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