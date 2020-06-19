package testCases.user;

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

public class TC_Profile extends TestBase{
	
	private String sessionId;

	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		String userId = "155";
		sessionId = setSession(userId);
		logger.info("***** END SET SESSION *****");
	}
	
	@Test
	void profileUser()
	{
		getProfile(sessionId);
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		
		if(code == 200)
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertNotEquals("", jsonPath.get("data.name"));
			Assert.assertNotEquals("", jsonPath.get("data.email"));
			Assert.assertNotEquals("", jsonPath.get("data.username"));
			checkEmailValid(jsonPath.get("data.email"));
			checkResultPhoneValid(jsonPath.get("data.username"));
			
			String query = "SELECT id, name, email, username FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(jsonPath.get("data.id").toString()));
			param.put("name", jsonPath.get("data.name"));
			param.put("email", jsonPath.get("data.email"));
			param.put("username", jsonPath.get("data.username"));
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(jsonPath.get("data.id").toString()), result.get("id"));
				Assert.assertEquals(jsonPath.get("data.name"), result.get("name"));
				Assert.assertEquals(jsonPath.get("data.email"), result.get("email"));
				Assert.assertEquals(jsonPath.get("data.username"), result.get("username"));
			}
		}
		else 
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"profileUser"})
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