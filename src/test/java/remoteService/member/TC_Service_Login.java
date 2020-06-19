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

public class TC_Service_Login extends TestBase{
	
	private String phone; 
	
	private JsonPath jsonPath = null;
	private String responseResult;
	
	public TC_Service_Login(String phone) {
		this.phone=phone;
	}

	@Test
	void loginUser()
	{
		String routingKey = "login";
		String message = "\""+phone+"\"";
		
		responseResult = callRP(memberAMQP, routingKey, message);
		jsonPath = new JsonPath(responseResult);
		
		logger.info("Test Data: ");
		logger.info("phone:" + phone);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"loginUser"})
	void checkResult()
	{	
		if(responseResult.startsWith("{\"id\""))
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("id").toString()));
			Assert.assertNotEquals("", jsonPath.get("name"));
			Assert.assertNotEquals("", jsonPath.get("email"));
			Assert.assertEquals(replacePhoneForAssertion(phone), jsonPath.get("username"));
			checkEmailValid(jsonPath.get("email"));
			checkResultPhoneValid(jsonPath.get("username"));
			
			String query = "SELECT id, name, email, username FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				Connection conMember = setConnection("MEMBER");
				PreparedStatement psGetUser = conMember.prepareStatement(query);
				psGetUser.setLong(1, Long.parseLong(jsonPath.get("id").toString()));
				psGetUser.setString(2, jsonPath.get("name"));
				psGetUser.setString(3, jsonPath.get("email"));
				psGetUser.setString(4, replacePhoneForAssertion(phone));
				
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