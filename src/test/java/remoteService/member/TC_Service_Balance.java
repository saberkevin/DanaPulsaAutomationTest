package remoteService.member;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;

import base.TestBase;

public class TC_Service_Balance extends TestBase{
	
	private String id;

	private String responseResult;

	public TC_Service_Balance(String id)
	{
		this.id = id;
	}
	
	public static boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        @SuppressWarnings("unused")
			Long d = Long.parseLong(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}

	@Test
	void balanceUser()
	{
		String routingKey = "getBalance";
		String message = id;
		
		responseResult = callRP(memberAMQP, routingKey, message);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"balanceUser"})
	void checkResult()
	{		
		if(isNumeric(responseResult.substring(1, responseResult.length()-1)))
		{
			responseResult = responseResult.substring(1, responseResult.length()-1);
			Assert.assertNotNull(responseResult);
			
			String query = "SELECT userId, balance FROM balance\n" + 
					"WHERE userId = ?";
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("userId", Long.parseLong(id));
			List<Map<String, Object>> responseResultSql = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResultSql) 
			{
				Assert.assertEquals(Long.parseLong(id), result.get("userId"));
				Assert.assertEquals(Long.parseLong(responseResult), result.get("balance"));
			}
		}
		else if(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.equals("user not found"))
		{
			Assert.assertTrue(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.equals("user not found"));
		}
		else 
		{
			logger.info("Test Data Error: ");
			logger.info("id:" + id);
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