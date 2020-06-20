package testCases.history;

import java.text.SimpleDateFormat;
import java.util.Date;
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

public class TC_History_Details extends TestBase{
	
	private String id; 
	private String sessionId;
	private String userId;
	
	public TC_History_Details(String id) {
		this.id=id;
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
	void historyDetailsUser()
	{
		String query = "SELECT id FROM transaction\n" + 
				"WHERE userId = ? ORDER BY createdAt DESC LIMIT 1";
		
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		param.put("userId",userId);
		
		List<Map<String, Object>> responseResult = sqlExec(query, param, "ORDER");
		
		for (Map<String, Object> result : responseResult) 
		{
			if(id.equals("valid")) id = String.valueOf(result.get("id"));
			else if(id.equals("-")) id = "-"+String.valueOf(result.get("id"));
		}	
		
		historyDetail(id,sessionId);
	}
	
	@Test(dependsOnMethods = {"historyDetailsUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{		
			String query = "SELECT a.id, b.name AS method, a.phoneNumber, a.catalogId, c.value, c.price, c.providerId, \n" + 
					"d.name AS provider, d.image, a.voucherId, \n" + 
					"f.name AS status, a.createdAt, a.updatedAt FROM transaction a\n" + 
					"JOIN payment_method b ON a.methodId = b.typeId\n" + 
					"JOIN pulsa_catalog c ON a.catalogId = c.id\n" + 
					"JOIN provider d ON c.providerId = d.id\n" + 
					"JOIN transaction_status f ON a.statusId = f.id\n" + 
					"WHERE a.userId = ? AND a.id = ?";
			
			String query2 = "SELECT name AS voucher, deduction, maxDeduction FROM voucher WHERE id = ? ";
			
			Assert.assertEquals("success", message);
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.id").toString()));
			Assert.assertNotEquals("", jsonPath.get("data.phoneNumber"));
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
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.catalog.id").toString()));
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.catalog.value").toString()));
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.catalog.price").toString()));
			Assert.assertNotNull(Long.parseLong(jsonPath.get("data.catalog.provider.id").toString()));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.provider.name"));
			Assert.assertNotEquals("", jsonPath.get("data.catalog.provider.image"));
			if(jsonPath.get("data.voucher.id") != null)
			{
				Assert.assertNotNull(Long.parseLong(jsonPath.get("data.voucher.id")));
				Assert.assertNotEquals("", jsonPath.get("data.catalog.voucher.name"));
				Assert.assertNotNull(Long.parseLong(jsonPath.get("data.voucher.deduction").toString()));
				Assert.assertNotNull(Long.parseLong(jsonPath.get("data.voucher.maxDeduction").toString()));
			}
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("userId",userId);
			param.put("id", id);
			
			List<Map<String, Object>> responseResult = sqlExec(query, param, "ORDER");
			
			for (Map<String, Object> result : responseResult) 
			{	    
				Date dateResultCreatedAt = new Date(jsonPath.getLong("data.createdAt"));
		        
		        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  
			    String resultCreatedAt= formatter.format(result.get("createdAt"));
			    String responseCreatedAt= formatter.format(dateResultCreatedAt);
			    
			    if((jsonPath.get("updatedAt") != null))
				{
			    	Date dateResultUpdatedAt = new Date(jsonPath.getLong("data.updatedAt"));
			    	String resultUpdatedAt= formatter.format(result.get("updatedAt"));
				    String responseUpdatedAt= formatter.format(dateResultUpdatedAt);
					Assert.assertEquals(resultUpdatedAt, responseUpdatedAt);	
				}
		        
				Assert.assertEquals(result.get("id"), Long.parseLong(jsonPath.get("data.id").toString()));
				Assert.assertEquals(result.get("method"), jsonPath.get("data.method"));
				Assert.assertEquals(result.get("phoneNumber"), jsonPath.get("data.phoneNumber"));
				Assert.assertEquals(result.get("catalogId"), Long.parseLong(jsonPath.get("data.catalog.id").toString()));
				Assert.assertEquals(result.get("value"), Long.parseLong(jsonPath.get("data.catalog.value").toString()));
				Assert.assertEquals(result.get("price"), Long.parseLong(jsonPath.get("data.catalog.price").toString()));
				Assert.assertEquals(result.get("providerId"), Long.parseLong(jsonPath.get("data.catalog.provider.id").toString()));
				Assert.assertEquals(result.get("provider"), jsonPath.get("data.catalog.provider.name"));
				Assert.assertEquals(result.get("image"), jsonPath.get("data.catalog.provider.image"));
				if(jsonPath.get("data.voucher.id") != null)
				{
					Assert.assertEquals(result.get("voucherId"), Long.parseLong(jsonPath.get("data.voucher.id").toString()));
				}
				Assert.assertEquals(result.get("status"), jsonPath.get("data.status"));
				Assert.assertEquals(resultCreatedAt, responseCreatedAt);
					
				if(jsonPath.get("data.voucher.id") != null)
				{	
					Map<String, Object> param2 = new LinkedHashMap<String, Object>();
					param2.put("userId",userId);
					param2.put("id", id);
					
					List<Map<String, Object>> responseResult2 = sqlExec(query2, param2, "PROMOTION");
					
					for (Map<String, Object> result2 : responseResult2) 
					{
						Assert.assertEquals(result2.get("voucher"), jsonPath.get("data.catalog.voucher.name"));
						Assert.assertEquals(result2.get("deduction"), Long.parseLong(jsonPath.get("data.voucher.deduction").toString()));
						Assert.assertEquals(result2.get("maxDeduction"), Long.parseLong(jsonPath.get("data.voucher.maxDeduction").toString()));
					}
				}	
			}
		}
		else if(code == 404)
		{
			Assert.assertEquals("unknown transaction", message);
		}
		else if(code == 400)
		{
			Assert.assertEquals("invalid request format", message);
		}
		else
		{
			Assert.assertTrue("unhandled error",false);
		}
	}
	
	@Test(dependsOnMethods = {"historyDetailsUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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
		logout(sessionId);
	}
}