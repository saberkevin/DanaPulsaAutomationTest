package testCases.history;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	void historyDetailsUser()
	{
		String query = "SELECT id FROM transaction\n" + 
				"WHERE userId = ? ORDER BY createdAt DESC LIMIT 1";
		try {
			Connection conOrder = getConnectionOrder();
			PreparedStatement psGetUserPin = conOrder.prepareStatement(query);
			psGetUserPin.setLong(1, Long.parseLong(userId));
			
			ResultSet result = psGetUserPin.executeQuery();
			
			while(result.next())
			{
				if(id.equals("valid")) id = String.valueOf(result.getLong("id"));
				else if(id.equals("-")) id = "-"+String.valueOf(result.getLong("id"));
			}
			
			conOrder.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			try { 
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
				Connection conOrder = getConnectionOrder();
				PreparedStatement psGetHistoryDetails = conOrder.prepareStatement(query);
				psGetHistoryDetails.setLong(1, Long.parseLong(userId));
				psGetHistoryDetails.setLong(2, Long.parseLong(jsonPath.get("data.id").toString()));
				ResultSet result = psGetHistoryDetails.executeQuery();
				
				while(result.next())
				{	    
					Date dateResultCreatedAt = new Date(jsonPath.getLong("data.createdAt"));
			        
			        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  
				    String resultCreatedAt= formatter.format(result.getDate("createdAt"));
				    String responseCreatedAt= formatter.format(dateResultCreatedAt);
				    
				    if((jsonPath.get("updatedAt") != null))
					{
				    	Date dateResultUpdatedAt = new Date(jsonPath.getLong("data.updatedAt"));
				    	String resultUpdatedAt= formatter.format(result.getDate("updatedAt"));
					    String responseUpdatedAt= formatter.format(dateResultUpdatedAt);
						Assert.assertEquals(resultUpdatedAt, responseUpdatedAt);	
					}
			        
					Assert.assertEquals(result.getLong("id"), Long.parseLong(jsonPath.get("data.id").toString()));
					Assert.assertEquals(result.getString("method"), jsonPath.get("data.method"));
					Assert.assertEquals(result.getString("phoneNumber"), jsonPath.get("data.phoneNumber"));
					Assert.assertEquals(result.getLong("catalogId"), Long.parseLong(jsonPath.get("data.catalog.id").toString()));
					Assert.assertEquals(result.getLong("value"), Long.parseLong(jsonPath.get("data.catalog.value").toString()));
					Assert.assertEquals(result.getLong("price"), Long.parseLong(jsonPath.get("data.catalog.price").toString()));
					Assert.assertEquals(result.getLong("providerId"), Long.parseLong(jsonPath.get("data.catalog.provider.id").toString()));
					Assert.assertEquals(result.getString("provider"), jsonPath.get("data.catalog.provider.name"));
					Assert.assertEquals(result.getString("image"), jsonPath.get("data.catalog.provider.image"));
					if(jsonPath.get("data.voucher.id") != null)
					{
						Assert.assertEquals(result.getLong("voucherId"), Long.parseLong(jsonPath.get("data.voucher.id").toString()));
					}
					Assert.assertEquals(result.getString("status"), jsonPath.get("data.status"));
					Assert.assertEquals(resultCreatedAt, responseCreatedAt);
						
					if(jsonPath.get("data.voucher.id") != null)
					{
						Connection conPromotion = getConnectionPromotion();
						PreparedStatement psGetVoucherName = conPromotion.prepareStatement(query2);
						psGetVoucherName.setLong(1, result.getLong("voucherId"));
						ResultSet resultVoucher = psGetVoucherName.executeQuery();
						
						while(resultVoucher.next())
						{
							Assert.assertEquals(resultVoucher.getString("voucher"), jsonPath.get("data.catalog.voucher.name"));
							Assert.assertEquals(resultVoucher.getLong("deduction"), Long.parseLong(jsonPath.get("data.voucher.deduction").toString()));
							Assert.assertEquals(resultVoucher.getLong("maxDeduction"), Long.parseLong(jsonPath.get("data.voucher.maxDeduction").toString()));
						}
						conPromotion.close();
					}	
				}
				conOrder.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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