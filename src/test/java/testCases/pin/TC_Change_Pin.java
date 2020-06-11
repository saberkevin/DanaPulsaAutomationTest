package testCases.pin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;
import model.User;

public class TC_Change_Pin extends TestBase{
	
	private String pin; 
	private User user;
	
	public TC_Change_Pin(String pin) {
		this.pin=pin;
	}

	@Test
	void changePinUser()
	{
		changePin(pin);
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
				PreparedStatement psGetUserPin = getConnectionMember().prepareStatement(query);
				psGetUserPin.setLong(1, Long.parseLong(user.getId()));
				psGetUserPin.setLong(1, Long.parseLong(pin));
				
				ResultSet result = psGetUserPin.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(user.getId(), result.getLong("id"));
					Assert.assertEquals(Long.parseLong(pin), result.getLong("pin"));
				}
				
				getConnectionMember().close();
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
		String sc = response.jsonPath().get("code");
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
	}
}