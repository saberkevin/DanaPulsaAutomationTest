package testCases.history;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_History_In_Progress extends TestBase{
	
	private String page;
	private String userId;
	private String sessionId;
	
	public TC_History_In_Progress(String page) {
		this.page = page;
	}
	
	@BeforeClass
	void setSession()
	{
		logger.info("***** SET SESSION *****");
		userId = "155";
		sessionId = setSession(userId);
		logger.info("***** END SET SESSION *****");
	}

	@Test
	void historyInProgressUser()
	{
		historyInProgress(page,sessionId);
	}
	
	@Test(dependsOnMethods = {"historyInProgressUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{
			String query = "SELECT a.id,a.userId,a.phoneNumber,d.price,a.voucherId,b.name AS status,a.createdAt FROM transaction a\n" + 
					"JOIN transaction_status b ON a.statusId = b.id AND b.typeId = 1\n" +  
					"JOIN pulsa_catalog d ON a.catalogId = d.id \n" +
					"WHERE a.userId = ? and a.id = ? \n" + 
					"ORDER BY a.createdAt DESC LIMIT 10 OFFSET ?";
			String query2 = "SELECT name AS voucher FROM voucher WHERE id = ? ";
			
			Assert.assertEquals("success", message);
			
			if(!jsonPath.get("data").toString().equals("[]"))
			{
				List<Map<String, String>> data = jsonPath.getList("data");
				
				for (int i = 0; i < data.size(); i++) {  
					Assert.assertNotNull(Long.parseLong(String.valueOf(data.get(i).get("id"))));
					Assert.assertNotEquals("", data.get(i).get("phoneNumber"));
					Assert.assertNotNull(Long.parseLong(String.valueOf(data.get(i).get("price"))));
					Assert.assertNotEquals("", data.get(i).get("voucher"));
					Assert.assertNotEquals("", data.get(i).get("createdAt"));
					Assert.assertTrue(data.get(i).get("status").equals("WAITING") || data.get(i).get("status").equals("VERIFYING"));
					
					Map<String, Object> param = new LinkedHashMap<String, Object>();
					param.put("userId",Long.parseLong(userId));
					param.put("id",Long.parseLong(String.valueOf(data.get(i).get("id"))));
					param.put("page", Long.parseLong(page)*10-10);
					List<Map<String, Object>> responseResult = sqlExec(query, param, "ORDER");
					
					for (Map<String, Object> result : responseResult) 
					{
						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  
					    String resultDate= formatter.format(result.get("createdAt"));
					    String responseDate= formatter.format(data.get(i).get("createdAt"));
					    
						Assert.assertEquals(result.get("id"), Long.parseLong(String.valueOf(data.get(i).get("id"))));
						Assert.assertEquals(result.get("phoneNumber"), data.get(i).get("phoneNumber"));
						Assert.assertEquals(result.get("price"), Long.parseLong(String.valueOf(data.get(i).get("price"))));
						Assert.assertEquals(result.get("status"), data.get(i).get("status"));
						Assert.assertEquals(resultDate, responseDate);
						
						Map<String, Object> param2 = new LinkedHashMap<String, Object>();
						param2.put("voucherId",Long.parseLong(result.get("voucherId").toString()));
						List<Map<String, Object>> responseResult2 = sqlExec(query2, param2, "PROMOTION");
						for (Map<String, Object> result2 : responseResult2) 
						{
							Assert.assertEquals(result2.get("voucher"), data.get(i).get("voucher"));
						}
					}
				}
			}	
		}
		else if(code == 400)
		{
			Assert.assertEquals("invalid request format",message);
		}
		else
		{
			Assert.assertTrue("unhandled error", false);
		}
	}
	
	@Test(dependsOnMethods = {"historyInProgressUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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
		logout(sessionId);
	}
}