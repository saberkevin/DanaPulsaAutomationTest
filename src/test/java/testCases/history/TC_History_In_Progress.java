package testCases.history;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_History_In_Progress extends TestBase{
	
	private String id; 
	
	public TC_History_In_Progress(String id) {
		this.id=id;
	}

	@Test
	void historyInProgressUser()
	{
		historyInProgress(id);
	}
	
	@Test(dependsOnMethods = {"historyInProgressUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			Assert.assertNotEquals("", jsonPath.get("data.id"));
			Assert.assertNotEquals("", jsonPath.get("data.phone"));
			Assert.assertNotEquals("", jsonPath.get("data.price"));
			Assert.assertNotEquals("", jsonPath.get("data.voucher"));
			Assert.assertNotEquals("", jsonPath.get("data.createdAt"));
			Assert.assertTrue(jsonPath.get("data.status").equals("WAITING") || jsonPath.get("data.status").equals("VERIFYING"));
		}
	}
	
	@Test(dependsOnMethods = {"historyInProgressUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"historyInProgressUser"})
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