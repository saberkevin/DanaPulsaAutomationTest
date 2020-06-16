package testCases.login;

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

public class TC_Logout extends TestBase{
	
	String sessionId;
	
	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		String userId = "155";
		String pinForSession = "";
		
		String query = "SELECT id, pin FROM user\n" + 
				"WHERE id = ?";
		try {
			Connection conMember = getConnectionMember();
			PreparedStatement psGetUserPin = conMember.prepareStatement(query);
			psGetUserPin.setLong(1, Long.parseLong(userId));
			
			ResultSet result = psGetUserPin.executeQuery();
			
			while(result.next())
			{
				pinForSession = result.getString("pin");
			}
			
			conMember.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		verifyPinLogin(userId, pinForSession);
		sessionId = response.getCookie("JSESSIONID");
		logger.info("***** END SET SESSION *****");
	}
	
	@Test
	void logoutUser()
	{
		logout(sessionId);
	}
	
	@Test(dependsOnMethods = {"logoutUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
		}
		else if(code == 404)
		{
			Assert.assertEquals("user session not found", message);
		}
	}
	
	@Test(dependsOnMethods = {"logoutUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"logoutUser"})
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