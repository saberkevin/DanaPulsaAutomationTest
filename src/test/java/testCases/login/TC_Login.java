package testCases.login;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(jsonPath.get("data.id").toString()));
			param.put("name", jsonPath.get("data.name"));
			param.put("email", jsonPath.get("data.email"));
			param.put("username", replacePhoneForAssertion(phone));
			
			List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");
			
			for (Map<String, Object> result : responseResult) 
			{
				Assert.assertEquals(Long.parseLong(jsonPath.get("data.id").toString()), result.get("id"));
				Assert.assertEquals(jsonPath.get("data.name"), result.get("name"));
				Assert.assertEquals(jsonPath.get("data.email"), result.get("email"));
				Assert.assertEquals(jsonPath.get("data.username"), result.get("username"));
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