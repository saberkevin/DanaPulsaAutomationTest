package testCases.otp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Change_Pin_Otp extends TestBase{

	private String sessionId;
	private String codeBefore;
	private String userId;
	
	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		userId = "155";
		String pinForSession = "";
		
		String query = "SELECT id, pin FROM user\n" + 
				"WHERE id = ?";
		try {
			Connection conMember = getConnectionMember();
			PreparedStatement psGetUserPin = conMember.prepareStatement(query);
			psGetUserPin.setLong(1, Long.parseLong(userId));
			
			ResultSet result = psGetUserPin.executeQuery();
			
			while(result.next())
			{
				pinForSession = result.getString("pin");
			}
			
			conMember.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		verifyPinLogin(userId, pinForSession);
		sessionId = response.getCookie("JSESSIONID");
		logger.info("***** END SET SESSION *****");
		
		logger.info("***** GET OTP Before *****");
		GetOtp(userId);
		if(response.getStatusCode() == 200)
		{
			codeBefore = response.jsonPath().get("data.code");
		}
		logger.info("***** END GET OTP Before *****");
	}
	
	@Test
	void changePinOtpUser()
	{
		changePinOtp(sessionId);
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
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.userId").toString()));
			Assert.assertTrue(!codeOtp.isEmpty());
			checkCodeValid(codeOtp);
			
			String query = "SELECT userId, code FROM otp\n" + 
					"WHERE userId = ?";
			try {
				Connection conMember = getConnectionMember();
				PreparedStatement psGetOtp= conMember.prepareStatement(query);
				psGetOtp.setLong(1, Long.parseLong(jsonPath.get("data.userId").toString()));
				
				ResultSet result = psGetOtp.executeQuery();
				
				while(result.next())
				{
					Assert.assertEquals(Long.parseLong(jsonPath.get("data.userId").toString()), result.getLong("userId"));
					Assert.assertEquals(codeOtp, result.getString("code"));
				}
				
				conMember.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			logger.info("***** Assert GET OTP After *****");
			GetOtp(userId);
			Assert.assertNotEquals(codeBefore, response.jsonPath().get("data.code"));
			logger.info("***** END Assert GET OTP After *****");
		}
		else if(code == 404)
		{
			Assert.assertEquals("user not found", message);
		}
		else if(code == 500)
		{
			Assert.assertTrue(message.equals("unverified number") || message.equals("invalid request format"));
		}
		else
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"changePinOtpUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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
		logout(sessionId);
	}
}