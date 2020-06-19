package integrationtest.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;
import model.User;

public class TC_IntegrationTest_001 extends TestBase {
	private User user = new User();
	
	JsonPath jsonPath;
	
	@Test
	void register()
	{
		user.setName("Kevin");
		user.setEmail("kevin@dana.id");
		user.setUsername("6282164886205");
		user.setPin(123456);
		String query = "DELETE FROM balance " + 
				"WHERE userId = ( " + 
				"SELECT tblTemp.id FROM (SELECT id FROM user WHERE email = ? OR username = ? LIMIT 1)tblTemp)";
		
		String query2 = "DELETE FROM user " + 
				"WHERE id = ( " + 
				"SELECT tblTemp.id FROM (SELECT id FROM user WHERE email = ? OR username = ? LIMIT 1)tblTemp)";
		
		try {
			Connection conUser = setConnection("MEMBER");
			PreparedStatement psDeleteBalance = conUser.prepareStatement(query);
			psDeleteBalance.setString(1, user.getEmail());
			psDeleteBalance.setString(2, user.getUsername());
			psDeleteBalance.executeUpdate();
			PreparedStatement psDeleteUser = conUser.prepareStatement(query2);
			psDeleteUser.setString(1, user.getEmail());
			psDeleteUser.setString(2, user.getUsername());
			psDeleteUser.executeUpdate();		
			conUser.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		register(user.getName(),user.getEmail(),user.getUsername(),String.valueOf(user.getPin()));
		
		jsonPath = response.jsonPath();
		
		if(response.getStatusCode() == 201)
		{
			user.setName(jsonPath.getString("data.name"));
			user.setEmail(jsonPath.getString("data.email"));
			user.setUsername(jsonPath.getString("data.username"));
			user.setId(jsonPath.getLong("data.id"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"register"})
	void login()
	{
		login(user.getUsername());
		
		jsonPath = response.jsonPath();
		
		if(response.getStatusCode() == 200)
		{
			user.setName(jsonPath.getString("data.name"));
			user.setEmail(jsonPath.getString("data.email"));
			user.setUsername(jsonPath.getString("data.username"));
			user.setId(jsonPath.getLong("data.id"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"register","login"})
	void verifyPinLogin1()
	{
		verifyPinLogin(String.valueOf(user.getId()), String.valueOf(user.getPin()));
		
		if(response.getStatusCode() == 200)
		{
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"register","login","verifyPinLogin1"})
	void changePin()
	{
		user.setPin(234567);
		changePin(String.valueOf(user.getPin()), user.getSessionId());
		
		if(response.getStatusCode() != 200)
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"register","login","verifyPinLogin1","changePin"})
	void logout1()
	{
		logout(user.getSessionId());
		
		if(response.getStatusCode() != 200)
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"register","login","verifyPinLogin1","changePin","logout1"})
	void verifyPinLogin2()
	{
		verifyPinLogin(String.valueOf(user.getId()), String.valueOf(user.getPin()));
		if(response.getStatusCode() == 200)
		{
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"register","login","verifyPinLogin1","changePin","logout1","verifyPinLogin2"})
	void profile()
	{
		getProfile(user.getSessionId());
		
		if(response.getStatusCode() == 200)
		{
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"register","login","verifyPinLogin1","changePin","logout1","verifyPinLogin2","profile"})
	void balance()
	{
		getBalance(user.getSessionId());
		
		if(response.getStatusCode() == 200)
		{
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	@Test(dependsOnMethods = {"register","login","verifyPinLogin1","changePin","logout1","verifyPinLogin2","profile","balance"})
	void logout2()
	{
		logout(user.getSessionId());
		
		if(response.getStatusCode() != 200)
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
}
