package testCases.history;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
				
				try {
					
					for (int i = 0; i < data.size(); i++) {  
						Assert.assertNotNull(Long.parseLong(String.valueOf(data.get(i).get("id"))));
						Assert.assertNotEquals("", data.get(i).get("phoneNumber"));
						Assert.assertNotNull(Long.parseLong(String.valueOf(data.get(i).get("price"))));
						Assert.assertNotEquals("", data.get(i).get("voucher"));
						Assert.assertNotEquals("", data.get(i).get("createdAt"));
						Assert.assertTrue(data.get(i).get("status").equals("WAITING") || data.get(i).get("status").equals("VERIFYING"));
						
						Connection conOrder = setConnection("ORDER");
						PreparedStatement psGetHistoryInProgress = conOrder.prepareStatement(query);
						psGetHistoryInProgress.setLong(1, Long.parseLong(userId));
						psGetHistoryInProgress.setLong(2, Long.parseLong(String.valueOf(data.get(i).get("id"))));
						psGetHistoryInProgress.setLong(3, Long.parseLong(page)*10-10);
						ResultSet result = psGetHistoryInProgress.executeQuery();
						
						while(result.next())
						{
							SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  
						    String resultDate= formatter.format(result.getDate("createdAt"));
						    String responseDate= formatter.format(data.get(i).get("createdAt"));
						    
							Assert.assertEquals(result.getLong("id"), Long.parseLong(String.valueOf(data.get(i).get("id"))));
							Assert.assertEquals(result.getString("phoneNumber"), data.get(i).get("phoneNumber"));
							Assert.assertEquals(result.getLong("price"), Long.parseLong(String.valueOf(data.get(i).get("price"))));
							Assert.assertEquals(result.getString("status"), data.get(i).get("status"));
							Assert.assertEquals(resultDate, responseDate);
							
							Connection conPromotion = setConnection("PROMOTION");
							PreparedStatement psGetVoucherName = conPromotion.prepareStatement(query2);
							psGetVoucherName.setLong(1, result.getLong("voucherId"));
							ResultSet resultVoucher = psGetVoucherName.executeQuery();
							
							while(resultVoucher.next())
							{
								Assert.assertEquals(result.getString("voucher"), data.get(i).get("voucher"));
							}
							conPromotion.close();
						}
						conOrder.close();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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