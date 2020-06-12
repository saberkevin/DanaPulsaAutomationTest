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
import model.User;

public class TC_Balance extends TestBase{
	
	private User user;

	@Test
	void balanceUser()
	{
		balanceUser();
	}
	
	@Test(dependsOnMethods = {"balanceUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.balance")));
			
			String query = "SELECT userId, balance FROM balance\n" + 
					"WHERE userId = ?";
			try {
				PreparedStatement psGetBalance = getConnectionMember().prepareStatement(query);
				psGetBalance.setLong(1, user.getId());
				
				ResultSet result = psGetBalance.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(user.getId(), result.getLong("userId"));
					Assert.assertEquals(Long.parseLong(jsonPath.get("data.balance")), result.getLong("balance"));
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
	
	@Test(dependsOnMethods = {"balanceUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
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