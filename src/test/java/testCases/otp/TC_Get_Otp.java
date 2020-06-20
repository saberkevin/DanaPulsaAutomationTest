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
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.userId").toString()));
			Assert.assertTrue(!jsonPath.get("data.code").toString().isEmpty());
			checkCodeValid(jsonPath.get("data.code"));
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(id));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(id), result.get("userId"));
				Assert.assertEquals(jsonPath.get("data.code"), result.get("code"));
			}
		}
		else if(code == 404)
		{
			Assert.assertTrue(message.contains("not found"));
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.equals("invalid request format"));
		}
		else
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"getOtpUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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