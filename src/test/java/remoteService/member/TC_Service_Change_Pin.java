package remoteService.member;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;


import base.TestBase;

public class TC_Service_Change_Pin extends TestBase{
	
	private String id;
	private String pin; 
	private String responseResult;
	
	public TC_Service_Change_Pin(String id, String pin) {
		this.id = id;
		this.pin=pin;
	}

	@Test
	void changePinUser()
	{
		String routingKey = "changePin";
		String message = "{\"id\":\""+id+"\",\"pin\":\""+pin+"\"}";
		
		responseResult = callRP(memberAMQP, routingKey, message);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("pin:" + pin);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
	void checkResult()
	{		
		if(responseResult.equals("updated"))
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
				Assert.assertEquals(pin, result.get("pin"));
			}
		}
		else if(responseResult.startsWith("invalid") || responseResult.equals("user not found") || responseResult.contains("should not be empty"))
		{
			Assert.assertTrue(responseResult.startsWith("invalid") || responseResult.equals("user not found") || responseResult.contains("should not be empty"));
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