package testCases.pin;

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

public class TC_Change_Pin extends TestBase{
	
	private String userId;
	private String pin; 
	private String sessionId;
	
	public TC_Change_Pin(String pin) {
		this.pin=pin;
	}
	
	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		userId = "155";
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
	void changePinUser()
	{
		changePin(pin,sessionId);
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("updated", message);
			
			String query = "SELECT id, pin FROM user\n" + 
					"WHERE id = ? AND pin = ?";
			try {
				Connection conMember = getConnectionMember();
				PreparedStatement psGetUserPin = conMember.prepareStatement(query);
				psGetUserPin.setLong(1, Long.parseLong(userId));
				psGetUserPin.setLong(2, Long.parseLong(pin));
				
				ResultSet result = psGetUserPin.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(userId), result.getLong("id"));
					Assert.assertEquals(Long.parseLong(pin), result.getLong("pin"));
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid"));
		}
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"changePinUser"})
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