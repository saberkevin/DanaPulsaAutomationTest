package remoteService.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
			try {
				Connection conMember = setConnection("MEMBER");
				PreparedStatement psGetUserPin = conMember.prepareStatement(query);
				psGetUserPin.setLong(1, Long.parseLong(id));
				psGetUserPin.setLong(2, Long.parseLong(pin));
				
				ResultSet result = psGetUserPin.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(id), result.getLong("id"));
					Assert.assertEquals(Long.parseLong(pin), result.getLong("pin"));
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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