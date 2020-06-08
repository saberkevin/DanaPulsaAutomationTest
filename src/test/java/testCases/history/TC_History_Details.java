package testCases.history;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_History_Details extends TestBase{
	
	private String id; 
	
	public TC_History_Details(String id) {
		this.id=id;
	}

	@Test
	void historyDetailsUser()
	{
		historyDetail(id);
	}
	
	@Test(dependsOnMethods = {"historyDetailsUser"})
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
			Assert.assertNotEquals("", jsonPath.get("data.method"));
			Assert.assertNotEquals("", jsonPath.get("data.updatedAt"));
			Assert.assertNotEquals("", jsonPath.get("data.createdAt"));
			Assert.assertTrue(jsonPath.get("data.status").equals("COMPLETED") || 
					jsonPath.get("data.status").equals("CANCELLED") || 
					jsonPath.get("data.status").equals("FAILED") || 
					jsonPath.get("data.status").equals("EXPIRED") ||
					jsonPath.get("data.status").equals("WAITING") ||
					jsonPath.get("data.status").equals("VERIFYING")
					);
			Assert.assertNotEquals("", jsonPath.get("data.catalog.id"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.value"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.price"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.provider.id"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.provider.name"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.provider.image"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.voucher.id"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.voucher.name"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.voucher.deduction"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.voucher.maxDeduction"));
		}
	}
	
	@Test(dependsOnMethods = {"historyDetailsUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"historyDetailsUser"})
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