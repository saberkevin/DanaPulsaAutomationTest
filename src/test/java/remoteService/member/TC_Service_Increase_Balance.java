package remoteService.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;

import base.TestBase;

public class TC_Service_Increase_Balance extends TestBase{
	
	private String id;
	private String value;
	private String balanceBefore;

	private String responseResult;

	public TC_Service_Increase_Balance(String id, String value)
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
	void increaseBalanceUser()
	{	
		balanceBefore = callRP(memberAMQP, "getBalance", id);
		if(isNumeric(balanceBefore.substring(1, balanceBefore.length()-1)))
		{
			balanceBefore = balanceBefore.substring(1, balanceBefore.length()-1);
		}
		String routingKey = "increaseBalance";
		String message = "{\"id\":\""+id+"\",\"value\":\""+value+"\"}";
		responseResult = callRP(memberAMQP, routingKey, message);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("value:" + value);
		logger.info("Balance Before:" + balanceBefore);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"increaseBalanceUser"})
	void checkResult()
	{		
		if(responseResult.equals("success"))
		{	
			String query = "SELECT userId, balance FROM balance\n" + 
					"WHERE userId = ?";
			try {
				Connection conMember = getConnectionMember();
				PreparedStatement psGetBalance = conMember.prepareStatement(query);
				psGetBalance.setLong(1, Long.parseLong(id));
				
				ResultSet result = psGetBalance.executeQuery();
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(id), result.getLong("userId"));
					Assert.assertTrue(result.getLong("balance")-Long.parseLong(balanceBefore) == Long.parseLong(value));
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.equals("user not found") || responseResult.equals("value should not be under zero"))
		{
			Assert.assertTrue(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.equals("user not found") || responseResult.equals("value should not be under zero"));
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