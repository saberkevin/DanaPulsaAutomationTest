package testCases.pin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Verify_Pin_Login extends TestBase{
	
	private String id;
	private String pin; 
	
	public TC_Verify_Pin_Login(String id, String pin) {
		this.id=id;
		this.pin=pin;
	}

	@Test
	void verifyPinLoginUser()
	{
		verifyPinLogin(id, pin);
	}
	
	@Test(dependsOnMethods = {"verifyPinLoginUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.getString("message");
		
		if(code == 200)
		{	
			String query = "SELECT id, pin FROM user\n" + 
					"WHERE id = ? AND pin = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(id));
			param.put("pin", Integer.parseInt(pin));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(id), result.get("id"));
				Assert.assertEquals(Integer.parseInt(pin), Integer.parseInt(result.get("pin").toString()));
			}
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid pin") || message.equals("invalid request format") || message.contains("must not be null"));
		}
		else if(code == 404)
		{
			Assert.assertTrue(message.contains("incorrect pin"));
		}
		else
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"verifyPinLoginUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"verifyPinLoginUser"})
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