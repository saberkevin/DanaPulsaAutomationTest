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
			try {
				Connection conMember = getConnectionMember();
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
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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