package testCases.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Login extends TestBase{
	
	private String phone; 
	
	public TC_Login(String phone) {
		this.phone=phone;
	}

	@Test
	void loginUser()
	{
		login(phone);
		System.out.println("phone");
	}
	
	@Test(dependsOnMethods = {"loginUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertNotEquals("", jsonPath.get("data.name"));
			Assert.assertNotEquals("", jsonPath.get("data.email"));
			Assert.assertEquals(replacePhoneForAssertion(phone), jsonPath.get("data.username"));
			checkEmailValid(jsonPath.get("data.email"));
			checkResultPhoneValid(jsonPath.get("data.username"));
			
			String query = "SELECT id, name, email, username FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				Connection conMember = getConnectionMember();
				PreparedStatement psGetUser = conMember.prepareStatement(query);
				psGetUser.setLong(1, Long.parseLong(jsonPath.get("data.id").toString()));
				psGetUser.setString(2, jsonPath.get("data.name"));
				psGetUser.setString(3, jsonPath.get("data.email"));
				psGetUser.setString(4, replacePhoneForAssertion(phone));
				
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
		else if(code == 400)
		{
			Assert.assertTrue(message.equals("invalid phone number"));
		}
		else if(code == 404)
		{
			Assert.assertTrue(message.equals("incorrect phone number"));
		}
		else
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"loginUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"loginUser"})
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