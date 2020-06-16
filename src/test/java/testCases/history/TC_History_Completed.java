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

public class TC_History_Completed extends TestBase{
	
	private String page;
	private String userId;
	private String sessionId;
	
	public TC_History_Completed(String page) {
		this.page = page;
	}
	
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
	}

	@Test
	void historyCompletedUser()
	{
		historyCompleted(page,sessionId);
	}
	
	@Test(dependsOnMethods = {"historyCompletedUser"})
	void checkResult()
	{
		int code = response.getStatusCode();
		JsonPath jsonPath = response.jsonPath();
		String message =  jsonPath.get("message");
		
		if(code == 200)
		{			
			String query = "SELECT a.id,a.userId,a.phoneNumber,d.price,a.voucherId ,b.name AS status,a.createdAt FROM transaction a\n" + 
					"JOIN transaction_status b ON a.statusId = b.id AND b.typeId = 2\n" +  
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
						Assert.assertNotNull(Long.parseLong(String.valueOf(data.get(i).get("id"))));
						Assert.assertNotEquals("", data.get(i).get("phoneNumber"));
						Assert.assertNotNull(Long.parseLong(String.valueOf(data.get(i).get("price"))));
						Assert.assertNotEquals("", data.get(i).get("voucher"));
						Assert.assertNotEquals("", data.get(i).get("createdAt"));
						Assert.assertTrue(data.get(i).get("status").equals("COMPLETED") || 
								data.get(i).get("status").equals("CANCELED") ||
								data.get(i).get("status").equals("EXPIRED") ||
								data.get(i).get("status").equals("FAILED") 
						);
						
						Connection conOrder = getConnectionOrder();
						PreparedStatement psGetHistoryCompleted = conOrder.prepareStatement(query);
						psGetHistoryCompleted.setLong(1, Long.parseLong(userId));
						psGetHistoryCompleted.setLong(2, Long.parseLong(String.valueOf(data.get(i).get("id"))));
						ResultSet result = psGetHistoryCompleted.executeQuery();
						
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
		else if(code == 500)
		{
			Assert.assertEquals("invalid request format",message);
		}
		else
		{
			Assert.assertTrue("unhandled error", false);
		}
	}
	
	@Test(dependsOnMethods = {"historyCompletedUser"})
	void assertStatusCode()
	{
		int sc = response.jsonPath().get("code");
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
		logout(sessionId);
	}
}