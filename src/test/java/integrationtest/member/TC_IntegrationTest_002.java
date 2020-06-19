package integrationtest.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.testng.Assert;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;
import model.User;

public class TC_IntegrationTest_002 extends TestBase {
	private User user = new User();
	private String code;
	JsonPath jsonPath;
	
	@Test
	void login()
	{
		user.setUsername("6282164886204");
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
	
	@Test(dependsOnMethods = {"login"})
	void verifyPinLogin()
	{
		user.setPin(123456);
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
	
	
	@Test(dependsOnMethods = {"login","verifyPinLogin"})
	void forgotPinOtp()
	{
		forgotPinOtp(String.valueOf(user.getId()));
		
		if(response.getStatusCode() != 200)
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp"})
	void getOtp()
	{
		GetOtp(String.valueOf(user.getId()));
		if(response.getStatusCode() == 200)
		{
			user.setId(response.jsonPath().getLong("data.userId"));
			code = response.jsonPath().getString("data.code");
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp"})
	void verifyOtp1()
	{
		verifyOtp(String.valueOf(user.getId()), code);
		
		if(response.getStatusCode() != 200)
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp","verifyOtp1"})
	void changePinOtp()
	{
		changePinOtp(user.getSessionId());
		if(response.getStatusCode() == 200)
		{
			user.setId(response.jsonPath().getLong("data.userId"));
			code = response.jsonPath().getString("data.code");
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp","verifyOtp1","changePinOtp"})
	void verifyOtp2()
	{
		verifyOtp(String.valueOf(user.getId()), code);
		
		if(response.getStatusCode() != 200)
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp","verifyOtp1","changePinOtp","verifyOtp2"})
	void historyInProgress()
	{
		historyInProgress("1", user.getSessionId());
		
		if(response.getStatusCode() == 200)
		{
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp","verifyOtp1","changePinOtp","verifyOtp2","historyInProgress"})
	void verifyPinLogin1()
	{
		user.setPin(123456);
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
	
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp","verifyOtp1","changePinOtp","verifyOtp2","historyInProgress","verifyPinLogin1"})
	void historyCompleted()
	{
		historyCompleted("1", user.getSessionId());
		
		if(response.getStatusCode() == 200)
		{
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp","verifyOtp1","changePinOtp","verifyOtp2","historyInProgress","verifyPinLogin1","historyCompleted"})
	void historyDetails()
	{
		long transactionId = 0;
		
		String query = "SELECT id FROM transaction\n" + 
				"WHERE userId = ? ORDER BY createdAt DESC LIMIT 1";
		try {
			Connection conOrder = getConnectionOrder();
			PreparedStatement psGetUserPin = conOrder.prepareStatement(query);
			psGetUserPin.setLong(1, user.getId());
			
			ResultSet result = psGetUserPin.executeQuery();
			
			while(result.next())
			{
				transactionId = result.getLong("id");
			};
			
			conOrder.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		historyDetail(String.valueOf(transactionId),user.getSessionId());
		
		if(response.getStatusCode() == 200)
		{
			user.setSessionId(response.getCookie("JSESSIONID"));
		}
		else
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
	
	@Test(dependsOnMethods = {"login","verifyPinLogin","forgotPinOtp","getOtp","verifyOtp1","changePinOtp","verifyOtp2","historyInProgress","verifyPinLogin1","historyCompleted","historyDetails"})
	void logout()
	{
		logout(user.getSessionId());
		
		if(response.getStatusCode() != 200)
		{
			Assert.assertTrue(false, "wrong status code (fail)");
		}
	}
}
