package testCases.history;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;
import model.User;

public class TC_History_In_Progress extends TestBase{
	
	private String page;
	private User user;
	
	public TC_History_In_Progress(String page) {
		this.page = page;
	}

	@Test
	void historyInProgressUser()
	{
		historyInProgress(page);
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
					"ORDER BY a.createdAt";
			String query2 = "SELECT name AS voucher FROM voucher WHERE id = ? ";
			
			Assert.assertEquals("success", message);
			
			if(!jsonPath.get("data").toString().equals("[]"))
			{
				List<Map<String, String>> data = jsonPath.getList("data");
				
				try {
					
					for (int i = 0; i < data.size(); i++) {  
						Assert.assertNotNull(Long.parseLong(data.get(i).get("id")));
						Assert.assertNotEquals("", data.get(i).get("phone"));
						Assert.assertNotNull(Long.parseLong(data.get(i).get("price")));
						Assert.assertNotEquals("", data.get(i).get("voucher"));
						Assert.assertNotEquals("", data.get(i).get("createdAt"));
						Assert.assertTrue(data.get(i).get("status").equals("WAITING") || data.get(i).get("status").equals("VERIFYING"));
						
						Connection conOrder = getConnectionOrder();
						PreparedStatement psGetHistoryInProgress = conOrder.prepareStatement(query);
						psGetHistoryInProgress.setLong(1, user.getId());
						psGetHistoryInProgress.setLong(2, Long.parseLong(data.get(i).get("id")));
						ResultSet result = psGetHistoryInProgress.executeQuery();
						
						while(result.next())
						{
							Assert.assertEquals(result.getLong("id"), data.get(i).get("id"));
							Assert.assertEquals(result.getString("phone"), data.get(i).get("phone"));
							Assert.assertEquals(result.getLong("price"), data.get(i).get("price"));
							Assert.assertEquals(result.getString("status"), data.get(i).get("status"));
							Assert.assertEquals(result.getDate("createdAt"), data.get(i).get("createdAt"));
							
							Connection conPromotion = getConnectionPromotion();
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