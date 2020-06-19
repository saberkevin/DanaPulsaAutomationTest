package remoteService.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Service_Register extends TestBase{
	
	private String name;
	private String email; 
	private String phone; 
	private String pin; 
	
	private JsonPath jsonPath = null;
	private String responseResult;
	
	public TC_Service_Register(String name, String email, String phone, String pin) {
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
			Connection conUser = getConnectionMember();
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
		
		String routingKey = "register";
		String message = "{\"name\":\""+name+"\",\"email\":\""+email+"\",\"phone\":\""+phone+"\",\"pin\":\""+pin+"\"}";
		
		responseResult = callRP(memberAMQP, routingKey, message);
		jsonPath = new JsonPath(responseResult);
		
		logger.info("Test Data: ");
		logger.info("name:" + name);
		logger.info("email:" + email);
		logger.info("phone:" + phone);
		logger.info("pin:" + pin);
		logger.info(responseResult);
	}
	
	@Test(dependsOnMethods = {"registerUser"})
	void checkResult()
	{	
		if(responseResult.startsWith("{\"id\""))
		{
			Assert.assertNotNull(Long.parseLong(jsonPath.get("id").toString()));
			Assert.assertEquals(name, jsonPath.get("name"));
			Assert.assertEquals(email, jsonPath.get("email"));
			Assert.assertEquals(replacePhoneForAssertion(phone), jsonPath.get("username"));
			checkEmailValid(jsonPath.get("email"));
			checkResultPhoneValid(jsonPath.get("username"));
			
			String query = "SELECT id, name, email, username, pin FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				Connection conUser = getConnectionMember();
				PreparedStatement psGetUser = conUser.prepareStatement(query);
				psGetUser.setLong(1, Long.parseLong(jsonPath.get("id").toString()));
				psGetUser.setString(2, jsonPath.get("name"));
				psGetUser.setString(3, jsonPath.get("email"));
				psGetUser.setString(4, jsonPath.get("username"));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("id").toString()), result.getLong("id"));
					Assert.assertEquals(jsonPath.get("name"), result.getString("name"));
					Assert.assertEquals(jsonPath.get("email"), result.getString("email"));
					Assert.assertEquals(jsonPath.get("username"), result.getString("username"));
					Assert.assertEquals(Long.parseLong(pin), result.getLong("pin"));
				}
				
				conUser.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(responseResult.startsWith("invalid") || responseResult.contains("should not be empty"))
		{	
			String query = "SELECT name, email, username, pin FROM user\n" + 
					"WHERE name = ?  AND email = ? AND username = ? AND pin = ?";
			try {
				Connection conUser = getConnectionMember();
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
		else if(responseResult.equals("user already exist"))
		{	
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
		else
		{
			Assert.assertTrue("Unhandled error",false);
			logger.info("Test Data For Error: ");
			logger.info("name:" + name);
			logger.info("email:" + email);
			logger.info("phone:" + phone);
			logger.info("pin:" + pin);
			logger.info(responseResult);
		}
	}
	
	@AfterClass
	void end()
	{
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}