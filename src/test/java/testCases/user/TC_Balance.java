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

public class TC_Balance extends TestBase{
	
	private String userId;
	private String sessionId;

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
	void balanceUser()
	{
		getBalance(sessionId);
	}
	
	@Test(dependsOnMethods = {"balanceUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		
		if(code == 200)
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data").toString()));
			
			String query = "SELECT userId, balance FROM balance\n" + 
					"WHERE userId = ?";
			try {
				Connection conMember = getConnectionMember();
				PreparedStatement psGetBalance = conMember.prepareStatement(query);
				psGetBalance.setLong(1, Long.parseLong(userId));
				
				ResultSet result = psGetBalance.executeQuery();
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(userId), result.getLong("userId"));
					Assert.assertEquals(Long.parseLong(jsonPath.get("data")), result.getLong("balance"));
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
	
	@Test(dependsOnMethods = {"balanceUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"balanceUser"})
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