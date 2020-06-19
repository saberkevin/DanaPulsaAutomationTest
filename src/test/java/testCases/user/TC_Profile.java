package testCases.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Profile extends TestBase{
	
	private String sessionId;

	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		String userId = "155";
		sessionId = setSession(userId);
		logger.info("***** END SET SESSION *****");
	}
	
	@Test
	void profileUser()
	{
		getProfile(sessionId);
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		
		if(code == 200)
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertNotEquals("", jsonPath.get("data.name"));
			Assert.assertNotEquals("", jsonPath.get("data.email"));
			Assert.assertNotEquals("", jsonPath.get("data.username"));
			checkEmailValid(jsonPath.get("data.email"));
			checkResultPhoneValid(jsonPath.get("data.username"));
			
			String query = "SELECT id, name, email, username FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				Connection conMember = setConnection("MEMBER");
				PreparedStatement psGetUser = conMember.prepareStatement(query);
				psGetUser.setLong(1, Long.parseLong(jsonPath.get("data.id").toString()));
				psGetUser.setString(2, jsonPath.get("data.name"));
				psGetUser.setString(3, jsonPath.get("data.email"));
				psGetUser.setString(4, jsonPath.get("data.username"));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("data.id").toString()), result.getLong("id"));
					Assert.assertEquals(jsonPath.get("data.name"), result.getString("name"));
					Assert.assertEquals(jsonPath.get("data.email"), result.getString("email"));
					Assert.assertEquals(jsonPath.get("data.username"), result.getString("username"));
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else 
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	@Parameters("responseTime")
	void assertResponseTime(String rt)
	{
		checkResponseTime(rt);
	}
	
	@AfterClass
	void end()
	{
		tearDown("Finished " + this.getClass().getSimpleName());
		logout(sessionId);
	}
}