package testCases.register;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		String query = "DELETE FROM user " + 
				"WHERE EXISTS ( " + 
				"SELECT * FROM (SELECT id FROM user WHERE email = ? OR username = ?)tblTemp)";
		String openRestrict= "SET FOREIGN_KEY_CHECKS=0";
		
		try {
			Connection conUser = getConnectionMember();
			Statement stmtRestrict = conUser.createStatement();
			stmtRestrict.execute(openRestrict);
			PreparedStatement psDeleteUser = conUser.prepareStatement(query);
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
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id")));
			Assert.assertEquals(name, jsonPath.get("data.name"));
			Assert.assertEquals(email, jsonPath.get("data.email"));
			Assert.assertEquals(replacePhoneForAssertion(phone), jsonPath.get("data.username"));
			checkEmailValid(jsonPath.get("data.email"));
			checkResultPhoneValid(jsonPath.get("data.username"));
			
			String query = "SELECT id, name, email, username, pin FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				Connection conUser = getConnectionMember();
				PreparedStatement psGetUser = conUser.prepareStatement(query);
				psGetUser.setLong(1, jsonPath.get("data.id"));
				psGetUser.setString(2, jsonPath.get("data.name"));
				psGetUser.setString(3, jsonPath.get("data.email"));
				psGetUser.setString(4, jsonPath.get("data.username"));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("data.id")), result.getLong("id"));
					Assert.assertEquals(jsonPath.get("data.name"), result.getString("name"));
					Assert.assertEquals(jsonPath.get("data.email"), result.getString("email"));
					Assert.assertEquals(jsonPath.get("data.username"), result.getString("username"));
					Assert.assertEquals(pin, result.getLong("pin"));
				}
				
				conUser.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("null"));
			
			String query = "SELECT name, email, username, pin FROM user\n" + 
					"WHERE name = ?  AND email = ? AND username = ? AND pin = ?";
			try {
				Connection conUser = getConnectionMember();
				PreparedStatement psGetUser = conUser.prepareStatement(query);
				psGetUser.setString(1, name);
				psGetUser.setString(2, email);
				psGetUser.setString(3, replacePhoneForAssertion(phone));
				psGetUser.setLong(4, Long.parseLong(pin));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(name, result.getString("name"));
					Assert.assertEquals(email, result.getString("email"));
					Assert.assertEquals(replacePhoneForAssertion(phone), result.getString("username"));
					Assert.assertEquals(pin, result.getLong("pin"));
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
				Connection conUser = getConnectionMember();
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
	}
	
	@Test(dependsOnMethods = {"registerUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
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