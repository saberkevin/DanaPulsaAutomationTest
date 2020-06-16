package remoteService.member;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;

public class TC_Service_Login extends TestBase{
	
	private String phone; 
	
	public TC_Service_Login(String phone) {
		this.phone=phone;
	}

	@SuppressWarnings("unchecked")
	@Test
	void loginUser()
	{
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("phone:" + phone);
		
		RestAssured.baseURI = memberURI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("queue", "login");
		requestParams.put("message", phone);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET);
		logger.info(response.getBody().asString());
	}
	
	@Test(dependsOnMethods = {"loginUser"})
	void checkResult()
	{
		String responseBody =  response.getBody().asString();
		
		if(responseBody.contains("{\"id\""))
		{
			JsonPath jsonPath = response.jsonPath();
			Assert.assertNotNull(Long.parseLong(jsonPath.get("id").toString()));
			Assert.assertNotEquals("", jsonPath.get("name"));
			Assert.assertNotEquals("", jsonPath.get("email"));
			Assert.assertEquals(replacePhoneForAssertion(phone), jsonPath.get("username"));
			checkEmailValid(jsonPath.get("email"));
			checkResultPhoneValid(jsonPath.get("username"));
			
			String query = "SELECT id, name, email, username FROM user\n" + 
					"WHERE id = ? AND name = ?  AND email = ? AND username = ?";
			try {
				PreparedStatement psGetUser = getConnectionMember().prepareStatement(query);
				psGetUser.setLong(1, Long.parseLong(jsonPath.get("id")));
				psGetUser.setString(2, jsonPath.get("name"));
				psGetUser.setString(3, jsonPath.get("email"));
				psGetUser.setString(4, replacePhoneForAssertion(phone));
				
				ResultSet result = psGetUser.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("id")), result.getLong("id"));
					Assert.assertEquals(jsonPath.get("name"), result.getString("name"));
					Assert.assertEquals(jsonPath.get("email"), result.getString("email"));
					Assert.assertEquals(jsonPath.get("username"), result.getString("username"));
				}
				
				getConnectionMember().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if(responseBody.contains("invalid") || responseBody.contains("incorrect"))
		{
			Assert.assertTrue(responseBody.equals("invalid phone number") || responseBody.equals("incorrect phone number"));
		}
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