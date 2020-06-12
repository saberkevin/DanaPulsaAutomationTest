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

public class TC_Verify_Otp extends TestBase{
	
	private String id;
	private String code;
	
	public TC_Verify_Otp(String id, String code) {
		this.id=id;
		this.code=code;
	}

	@Test
	void verifyOtpUser()
	{
		GetOtp(id);
		
		String otp = response.jsonPath().get("data.code");
		
		if(code.equals("valid"))
		{
			code = otp;
		}
		else if(code.equals("-"))
		{
			code = "-"+otp;
		}
		else if(code.equals("3digit"))
		{
			code = otp.substring(0, 2);
		}
		else if(code.equals("5digit"))
		{
			code = otp+"5";
		}
		
		
		verifyOtp(id, code);
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			Assert.assertNotEquals("", jsonPath.get("data.token"));
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			try {
				PreparedStatement psGetOtp= getConnectionMember().prepareStatement(query);
				psGetOtp.setLong(1, Long.parseLong(id));
				
				ResultSet result = psGetOtp.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(id), result.getLong("userId"));
					Assert.assertEquals(this.code, result.getString("code"));
				}
				
				getConnectionMember().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(code == 404)
		{
			Assert.assertEquals("incorrect OTP", message);
		}
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"verifyOtpUser"})
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