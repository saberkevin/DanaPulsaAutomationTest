package testCases.register;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(jsonPath.get("data.id").toString()));
			param.put("name", jsonPath.get("data.name"));
			param.put("email", jsonPath.get("data.email"));
			param.put("username", jsonPath.get("data.username"));
			
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(jsonPath.get("data.id").toString()), result.get("id"));
				Assert.assertEquals(jsonPath.get("data.name"), result.get("name"));
				Assert.assertEquals(jsonPath.get("data.email"), result.get("email"));
				Assert.assertEquals(jsonPath.get("data.username"), result.get("username"));
				Assert.assertEquals(pin, result.get("pin"));
			}
		}
		else if(code == 400)
		{
			Assert.assertTrue(message.contains("invalid") || message.contains("must not be null"));
			
			String query = "SELECT name, email, username, pin FROM user\n" + 
					"WHERE name = ?  AND email = ? AND username = ? AND pin = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("name", name);
			param.put("email", email);
			param.put("username", replacePhoneForAssertion(phone));
			param.put("pin", pin);
			
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
				
			for (@SuppressWarnings("unused") Map<String, Object> result : responseResult) 
			{
				Assert.assertTrue("should not exists in database", false);
			}
		}
		else if(code == 409)
		{
			Assert.assertEquals("user already exists", message);
			
			String query = "SELECT COUNT(id) as count FROM user\n" + 
					"WHERE name = ?  AND email = ? AND username = ? AND pin = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("name", name);
			param.put("email", email);
			param.put("username", replacePhoneForAssertion(phone));
			param.put("pin", Long.parseLong(pin));
			
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(1, result.get("count"));
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