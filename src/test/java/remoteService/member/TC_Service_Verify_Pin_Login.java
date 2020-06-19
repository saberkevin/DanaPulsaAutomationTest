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

public class TC_Service_Verify_Pin_Login extends TestBase{
	
	private String id;
	private String pin; 
	
	private JsonPath jsonPath = null;
	private String responseResult;
	
	public TC_Service_Verify_Pin_Login(String id, String pin) {
		this.id=id;
		this.pin=pin;
	}

	@Test
	void verifyPinLoginUser()
	{
		String routingKey = "verifyPin";
		String message = "{\"id\":\""+id+"\",\"pin\":\""+pin+"\"}";
		
		responseResult = callRP(memberAMQP, routingKey, message);
		jsonPath = new JsonPath(responseResult);
		
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("pin:" + pin);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"verifyPinLoginUser"})
	void checkResult()
	{	
		if(responseResult.startsWith("{\"id\""))
		{	
			Assert.assertEquals(id,jsonPath.get("id").toString());
			Assert.assertNotEquals("", jsonPath.get("name"));
			Assert.assertNotEquals("", jsonPath.get("email"));
			Assert.assertNotEquals("", jsonPath.get("username"));
			checkEmailValid(jsonPath.get("email"));
			checkResultPhoneValid(jsonPath.get("username"));
			checkPinValid(pin);
			
			String query = "SELECT id, name, email, username, pin FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				Connection conMember = setConnection("MEMBER");
				PreparedStatement psGetUser = conMember.prepareStatement(query);
				psGetUser.setLong(1, Long.parseLong(jsonPath.get("id").toString()));
				psGetUser.setString(2, jsonPath.get("name"));
				psGetUser.setString(3, jsonPath.get("email"));
				psGetUser.setString(4, jsonPath.get("username"));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("id").toString()), result.getLong("id"));
					Assert.assertEquals(jsonPath.get("name"), result.getString("name"));
					Assert.assertEquals(jsonPath.get("email"), result.getString("email"));
					Assert.assertEquals(jsonPath.get("username"), result.getString("username"));
					Assert.assertEquals(pin, result.getString("pin"));
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.startsWith("incorrect"))
		{
			Assert.assertTrue(responseResult.contains("should not be empty") || responseResult.startsWith("invalid") || responseResult.startsWith("incorrect"));
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