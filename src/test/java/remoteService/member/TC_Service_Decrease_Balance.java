package remoteService.member;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;

import base.TestBase;

public class TC_Service_Decrease_Balance extends TestBase{
	
	private String id;
	private String value;
	private String balanceBefore;

	private String responseResult;

	public TC_Service_Decrease_Balance(String id, String value)
	{
		this.id = id;
		this.value = value;
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
	void decreaseBalanceUser()
	{	
		balanceBefore = callRP(memberAMQP, "getBalance", id);
		if(isNumeric(balanceBefore.substring(1, balanceBefore.length()-1)))
		{
			balanceBefore = balanceBefore.substring(1, balanceBefore.length()-1);
		}
		String routingKey = "decreaseBalance";
		String message = "{\"id\":\""+id+"\",\"value\":\""+value+"\"}";
		responseResult = callRP(memberAMQP, routingKey, message);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("value:" + value);
		logger.info("Balance Before:" + balanceBefore);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"decreaseBalanceUser"})
	void checkResult()
	{		
		if(responseResult.equals("\"success\""))
		{	
			String query = "SELECT userId, balance FROM balance\n" + 
					"WHERE userId = ?";
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("userId", Long.parseLong(id));
			List<Map<String, Object>> responseResultSql = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResultSql) 
			{
				Assert.assertEquals(Long.parseLong(id), result.get("userId"));
				Assert.assertTrue(Long.parseLong(balanceBefore)-Long.parseLong(result.get("balance").toString()) == Long.parseLong(value));
			}
		}
		else if(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.equals("user not found") || responseResult.equals("not enough balance") || responseResult.equals("value should not be under zero"))
		{
			Assert.assertTrue(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.equals("user not found") || responseResult.equals("not enough balance") || responseResult.equals("value should not be under zero"));
		}
		else 
		{
			logger.info("Test Data Error: ");
			logger.info("id:" + id);
			logger.info("value:" + value);
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