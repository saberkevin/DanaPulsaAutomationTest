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

public class TC_Forgot_Pin_Otp extends TestBase{
	
	private String id; 
	
	public TC_Forgot_Pin_Otp(String id) {
		this.id=id;
	}

	@Test
	void forgotPinOtpUser()
	{
		forgotPinOtp(id);
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ? AND  ((TIME_TO_SEC(NOW())-TIME_TO_SEC(TIME(updatedAt))/60) <= 5";
			try {
				PreparedStatement psGetOtp= getConnectionMember().prepareStatement(query);
				psGetOtp.setLong(1, Long.parseLong(id));
				
				ResultSet result = psGetOtp.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(id), result.getLong("userId"));
					Assert.assertNotNull(result.getString("code"));
				}
				
				getConnectionMember().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"forgotPinOtpUser"})
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