package testCases.pin;

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

public class TC_Change_Pin extends TestBase{
	
	private String userId;
	private String pin; 
	private String sessionId;
	
	public TC_Change_Pin(String pin) {
		this.pin=pin;
	}
	
	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		userId = "155";
		sessionId = setSession(userId);
		logger.info("***** END SET SESSION *****");
	}

	@Test
	void changePinUser()
	{
		changePin(pin,sessionId);
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("updated", message);
			
			String query = "SELECT id, pin FROM user\n" + 
					"WHERE id = ? AND pin = ?";
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(userId));
			param.put("pin", Integer.parseInt(pin));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(userId), result.get("id"));
				Assert.assertEquals(Integer.parseInt(pin), Integer.parseInt(result.get("pin").toString()));
			}
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid"));
		}
		else
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
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