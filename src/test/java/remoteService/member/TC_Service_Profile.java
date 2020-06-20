package remoteService.member;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Service_Profile extends TestBase{
	
	private String id;
	
	private JsonPath jsonPath = null;
	private String responseResult;

	public TC_Service_Profile(String id)
	{
		this.id = id;
	}
	
	@Test
	void profileUser()
	{
		String routingKey = "getProfile";
		String message = id;
		
		responseResult = callRP(memberAMQP, routingKey, message);
		jsonPath = new JsonPath(responseResult);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	void checkResult()
	{	
		if(responseResult.startsWith("{\"id\""))
		{
			Assert.assertNotNull(id, jsonPath.get("id").toString());
			Assert.assertNotEquals("", jsonPath.get("name"));
			Assert.assertNotEquals("", jsonPath.get("email"));
			Assert.assertNotEquals("", jsonPath.get("username"));
			checkEmailValid(jsonPath.get("email"));
			checkResultPhoneValid(jsonPath.get("username"));
			
			String query = "SELECT id, name, email, username FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(jsonPath.get("id").toString()));
			param.put("name", jsonPath.get("name"));
			param.put("email", jsonPath.get("email"));
			param.put("username", jsonPath.get("username"));
			
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(jsonPath.get("id").toString()), result.get("id"));
				Assert.assertEquals(jsonPath.get("name"), result.get("name"));
				Assert.assertEquals(jsonPath.get("email"), result.get("email"));
				Assert.assertEquals(jsonPath.get("username"), result.get("username"));
			}
		}
		else if(responseResult.contains("should not be empty") || responseResult.contains("invalid") || responseResult.equals("user not found"))
		{
			Assert.assertTrue(responseResult.contains("should not be empty") || responseResult.contains("invalid") || responseResult.equals("user not found"));
		}
		else 
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@AfterClass
	void end()
	{
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}