package testCases.otp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Change_Pin_Otp extends TestBase{

	private String sessionId;
	private String codeBefore;
	private String userId;
	
	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		userId = "155";
		sessionId = setSession(userId);
		logger.info("***** END SET SESSION *****");
		
		logger.info("***** GET OTP Before *****");
		GetOtp(userId);
		if(response.getStatusCode() == 200)
		{
			codeBefore = response.jsonPath().get("data.code");
		}
		logger.info("***** END GET OTP Before *****");
	}
	
	@Test
	void changePinOtpUser()
	{
		changePinOtp(sessionId);
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		String codeOtp = jsonPath.get("data.code");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.userId").toString()));
			Assert.assertTrue(!codeOtp.isEmpty());
			checkCodeValid(codeOtp);
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(jsonPath.get("data.userId").toString()));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(jsonPath.get("data.userId").toString()), result.get("userId"));
				Assert.assertEquals(codeOtp, result.get("code"));
			}
			
			logger.info("***** Assert GET OTP After *****");
			GetOtp(userId);
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
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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
		logout(sessionId);
	}
}