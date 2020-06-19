package remoteService.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Service_Send_Otp extends TestBase{
	
	private String id; 
	
	private String responseResult;
	private JsonPath jsonPath = null;
	
	public TC_Service_Send_Otp(String id) {
		this.id=id;
	}

	@Test
	void sendOtpUser()
	{
		String routingKey = "sendOTP";
		String message = id;
		
		responseResult = callRP(memberAMQP, routingKey, message);
		
		jsonPath = new JsonPath(responseResult);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"sendOtpUser"})
	void checkResult()
	{	
		if(responseResult.startsWith("{\"id\""))
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("id").toString()));
			Assert.assertEquals(id, jsonPath.get("userId").toString());
			Assert.assertTrue(!jsonPath.get("code").toString().isEmpty());
			checkCodeValid(jsonPath.get("code"));
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			try {
				Connection conMember = setConnection("MEMBER");
				PreparedStatement psGetOtp= setConnection("MEMBER").prepareStatement(query);
				psGetOtp.setLong(1, Long.parseLong(id));
				
				ResultSet result = psGetOtp.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(id), result.getLong("userId"));
					Assert.assertEquals(jsonPath.get("code"), result.getString("code"));
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.contains("not found") || responseResult.equals("unverified number"))
		{
			Assert.assertTrue(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.contains("not found") || responseResult.equals("unverified number"));
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