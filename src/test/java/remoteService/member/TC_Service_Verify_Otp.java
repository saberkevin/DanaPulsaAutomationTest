package remoteService.member;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Service_Verify_Otp extends TestBase{
	
	private String id;
	private String code;
	
	private String responseResult;
	private JsonPath jsonPath = null;
	
	public TC_Service_Verify_Otp(String id, String code) {
		this.id=id;
		this.code=code;
	}

	@Test
	void verifyOtpUser()
	{
		String otp = "";
		logger.info("***** GET OTP *****");
		String responseGetOtp = callRP(memberAMQP, "getOTP", id);
		if(responseGetOtp.startsWith("{\"id\""))
		{
			JsonPath jsonPathGetOtp = new JsonPath(responseGetOtp);
			otp = jsonPathGetOtp.get("code");
		}
		
		logger.info("***** END GET OTP *****");
		
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
		
		String routingKey = "verifyOTP";
		String message = "{\"id\":\""+id+"\",\"code\":\""+code+"\"}";
		
		responseResult = callRP(memberAMQP, routingKey, message);
		
		jsonPath = new JsonPath(responseResult);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("code:" + code);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
	void checkResult()
	{	
		if(responseResult.startsWith("{\"id\""))
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("id").toString()));
			Assert.assertEquals(id, jsonPath.get("userId").toString());
			Assert.assertEquals(code, jsonPath.get("code").toString());
			checkCodeValid(jsonPath.get("code"));
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(id));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(jsonPath.get("userId").toString()), result.get("userId"));
				Assert.assertEquals(jsonPath.get("code"), result.get("code"));
			}
		}
		else if(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.startsWith("incorrect") || responseResult.equals("OTP expired"))
		{
			Assert.assertTrue(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.startsWith("incorrect") || responseResult.equals("OTP expired"));
		}
		else 
		{
			logger.info("Test Data Error: ");
			logger.info("id:" + id);
			logger.info("code:" + code);
			logger.info(responseResult);
			Assert.assertTrue("unhandled error",false);	
		}
	}
	@AfterClass
	void end()
	{
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}