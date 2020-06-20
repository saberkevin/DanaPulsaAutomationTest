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
		logger.info("***** GET OTP *****");
		GetOtp(id);
		logger.info("***** END GET OTP *****");
		String otp = response.jsonPath().get("data.code");
		
		if(code.equals("valid"))
		{
			code = otp;
		}
		else if(code.equals("-"))
		{
			code = "-"+otp;
		}
		else if(code.equals("3digit"))
		{
			code = otp.substring(0, 2);
		}
		else if(code.equals("5digit"))
		{
			code = otp+"5";
		}
		
		
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
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(id));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(id), result.get("userId"));
				Assert.assertEquals(this.code, result.get("code"));
			}
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.equals("invalid OTP") || message.contains("must not be null") || message.equals("invalid request format"));
		}
		else if(code == 404)
		{
			Assert.assertEquals("incorrect OTP", message);
		}
		else if(code == 401)
		{
			Assert.assertEquals("OTP expired", message);
		}
		else
		{
			Assert.assertTrue("unhandled error", false);
		}
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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