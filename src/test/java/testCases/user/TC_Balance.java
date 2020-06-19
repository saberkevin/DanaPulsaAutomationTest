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
		sessionId = setSession(userId);
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
				Connection conMember = setConnection("MEMBER");
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
		logout(sessionId);
	}
}