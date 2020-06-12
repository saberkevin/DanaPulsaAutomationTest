package testCases.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Profile extends TestBase{

	@Test
	void profileUser()
	{
		getProfile();
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id")));
			Assert.assertNotEquals("", jsonPath.get("data.name"));
			Assert.assertNotEquals("", jsonPath.get("data.email"));
			Assert.assertNotEquals("", jsonPath.get("data.username"));
			checkEmailValid(jsonPath.get("data.email"));
			checkResultPhoneValid(jsonPath.get("data.username"));
			
			String query = "SELECT id, name, email, username FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				PreparedStatement psGetUser = getConnectionMember().prepareStatement(query);
				psGetUser.setLong(1, jsonPath.get("data.id"));
				psGetUser.setString(2, jsonPath.get("data.name"));
				psGetUser.setString(3, jsonPath.get("data.email"));
				psGetUser.setString(4, jsonPath.get("data.username"));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("data.id")), result.getLong("id"));
					Assert.assertEquals(jsonPath.get("data.name"), result.getString("name"));
					Assert.assertEquals(jsonPath.get("data.email"), result.getString("email"));
					Assert.assertEquals(jsonPath.get("data.username"), result.getString("username"));
				}
				
				getConnectionMember().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(code == 404)
		{
			Assert.assertEquals("user not found", message);
		}
	}
	
	@Test(dependsOnMethods = {"profileUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
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
	}
}