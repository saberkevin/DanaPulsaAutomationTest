package testCases.otp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Change_Pin_Otp extends TestBase{

	@Test
	void changePinOtpUser()
	{
		changePinOtp();
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		String codeOtp = jsonPath.get("data.code");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id")));
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.userId")));
			Assert.assertNotEquals("", codeOtp);
			checkCodeValid(codeOtp);
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			try {
				PreparedStatement psGetOtp= getConnectionMember().prepareStatement(query);
				psGetOtp.setLong(1, Long.parseLong(jsonPath.get("data.userId")));
				
				ResultSet result = psGetOtp.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("data.userId")), result.getLong("userId"));
					Assert.assertEquals(codeOtp, result.getString("code"));
				}
				
				getConnectionMember().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			GetOtp(jsonPath.get("data.userId"));
			Assert.assertEquals(codeOtp, response.jsonPath().get("data.code"));
			
		}
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
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