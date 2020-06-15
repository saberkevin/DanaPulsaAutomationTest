package remoteService.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;

public class TC_Service_Register extends TestBase{
	
	private String name;
	private String email; 
	private String phone; 
	private String pin; 
	
	public TC_Service_Register(String name, String email, String phone, String pin) {
		this.name=name;
		this.email=email;
		this.phone=phone;
		this.pin=pin;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void registerUser()
	{	
		String query = "DELETE FROM user " + 
				"WHERE id = ( " + 
				"SELECT tblTemp.id FROM (SELECT id FROM user WHERE email = ? OR username = ? LIMIT 1)tblTemp)";
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
		
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("name:" + name);
		logger.info("email:" + email);
		logger.info("phone:" + phone);
		logger.info("pin:" + pin);
		
		RestAssured.baseURI = memberURI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("queue", "register");
		requestParams.put("message", "{\"name\":\""+name+"\",\"email\":\""+email+"\",\"phone\":\""+phone+"\",\"pin\":"+pin+"}");
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET);
		logger.info(response.getBody().asString());
	}
	
	@Test(dependsOnMethods = {"registerUser"})
	void checkResult()
	{
		String responseBody = response.getBody().asString();
		JsonPath jsonPath = response.jsonPath();
		
		if(responseBody.contains("invalid"))
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
		else if(responseBody.equals("user already exists"))
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
		else if(responseBody.contains("{\"id\""))
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