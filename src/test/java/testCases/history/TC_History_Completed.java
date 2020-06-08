package testCases.history;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_History_Completed extends TestBase{
	
	private String id; 
	
	public TC_History_Completed(String id) {
		this.id=id;
	}

	@Test
	void historyCompletedUser()
	{
		historyCompleted(id);
	}
	
	@Test(dependsOnMethods = {"historyCompletedUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			Assert.assertEquals("success", message);
			
			if(!jsonPath.get("data").toString().equals("[]"))
			{
				List<Map<String, String>> data = jsonPath.getList("data");
				
				for (int i = 0; i < data.size(); i++)
				{
					Assert.assertNotEquals("", data.get(i).get(id));
					Assert.assertNotEquals("", data.get(i).get("phone"));
					Assert.assertNotEquals("", data.get(i).get("price"));
					Assert.assertNotEquals("", data.get(i).get("voucher"));
					Assert.assertNotEquals("", data.get(i).get("createdAt"));
					Assert.assertTrue(data.get(i).get("status").equals("COMPLETED") || 
							data.get(i).get("status").equals("CANCELED") ||
							data.get(i).get("status").equals("EXPIRED") ||
							data.get(i).get("status").equals("FAILED") 
					);
				}
			}
		}
	}
	
	@Test(dependsOnMethods = {"historyCompletedUser"})
	void assertStatusCode()
	{
		String sc = response.jsonPath().get("code");
		checkStatusCode(sc);	
	}
	
	@Test(dependsOnMethods = {"historyCompletedUser"})
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