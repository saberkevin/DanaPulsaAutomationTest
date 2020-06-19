package testCases.register;

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

public class TC_Register extends TestBase{
	
	private String name;
	private String email; 
	private String phone; 
	private String pin; 
	
	public TC_Register(String name, String email, String phone, String pin) {
		this.name=name;
		this.email=email;
		this.phone=phone;
		this.pin=pin;
	}
	
	@Test
	void registerUser()
	{	
		String query = "DELETE FROM balance " + 
				"WHERE userId = ( " + 
				"SELECT tblTemp.id FROM (SELECT id FROM user WHERE email = ? OR username = ? LIMIT 1)tblTemp)";
		
		String query2 = "DELETE FROM user " + 
				"WHERE id = ( " + 
				"SELECT tblTemp.id FROM (SELECT id FROM user WHERE email = ? OR username = ? LIMIT 1)tblTemp)";
		
		try {
			Connection conUser = setConnection("MEMBER");
			PreparedStatement psDeleteBalance = conUser.prepareStatement(query);
			psDeleteBalance.setString(1, email);
			psDeleteBalance.setString(2, replacePhoneForAssertion(phone));
			psDeleteBalance.executeUpdate();
			PreparedStatement psDeleteUser = conUser.prepareStatement(query2);
			psDeleteUser.setString(1, email);
			psDeleteUser.setString(2, replacePhoneForAssertion(phone));
			psDeleteUser.executeUpdate();		
			conUser.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		register(name,email,phone,pin);
	}
	
	@Test(dependsOnMethods = {"registerUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.getString("message");
		
		if(code == 201)
		{
			Assert.assertEquals("created", message);
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertEquals(name, jsonPath.get("data.name"));
			Assert.assertEquals(email, jsonPath.get("data.email"));
			Assert.assertEquals(replacePhoneForAssertion(phone), jsonPath.get("data.username"));
			checkEmailValid(jsonPath.get("data.email"));
			checkResultPhoneValid(jsonPath.get("data.username"));
			
			String query = "SELECT id, name, email, username, pin FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				Connection conUser = setConnection("MEMBER");
				PreparedStatement psGetUser = conUser.prepareStatement(query);
				psGetUser.setLong(1, Long.parseLong(jsonPath.get("data.id").toString()));
				psGetUser.setString(2, jsonPath.get("data.name"));
				psGetUser.setString(3, jsonPath.get("data.email"));
				psGetUser.setString(4, jsonPath.get("data.username"));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("data.id").toString()), result.getLong("id"));
					Assert.assertEquals(jsonPath.get("data.name"), result.getString("name"));
					Assert.assertEquals(jsonPath.get("data.email"), result.getString("email"));
					Assert.assertEquals(jsonPath.get("data.username"), result.getString("username"));
					Assert.assertEquals(Long.parseLong(pin), result.getLong("pin"));
				}
				
				conUser.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid") || message.contains("must not be null"));
			
			String query = "SELECT name, email, username, pin FROM user\n" + 
					"WHERE name = ?  AND email = ? AND username = ? AND pin = ?";
			try {
				Connection conUser = setConnection("MEMBER");
				PreparedStatement psGetUser = conUser.prepareStatement(query);
				psGetUser.setString(1, name);
				psGetUser.setString(2, email);
				psGetUser.setString(3, replacePhoneForAssertion(phone));
				psGetUser.setString(4, pin);
				
				ResultSet result = psGetUser.executeQuery();
				
				if(result.next())
				{
					Assert.assertTrue("should not exists in database", false);
				}
				
				conUser.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(code == 409)
		{
			Assert.assertEquals("user already exists", message);
			
			String query = "SELECT COUNT(id) as count FROM user\n" + 
					"WHERE name = ?  AND email = ? AND username = ? AND pin = ?";
			try {
				Connection conUser = setConnection("MEMBER");
				PreparedStatement psGetUser = conUser.prepareStatement(query);
				psGetUser.setString(1, name);
				psGetUser.setString(2, email);
				psGetUser.setString(3, replacePhoneForAssertion(phone));
				psGetUser.setLong(4, Long.parseLong(pin));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(1, result.getInt("count"));
				}
				
				conUser.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			Assert.assertTrue("Unhandled error",false);
			logger.info("Test Data For Error: ");
			logger.info("name:" + name);
			logger.info("email:" + email);
			logger.info("phone:" + phone);
			logger.info("pin:" + pin);
			logger.info(response.getBody().asString());
		}
	}
	
	@Test(dependsOnMethods = {"registerUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"registerUser"})
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