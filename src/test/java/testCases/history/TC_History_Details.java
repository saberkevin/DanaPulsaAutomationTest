package testCases.history;

import java.sql.DriverManager;
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

public class TC_History_Details extends TestBase{
	
	private String id; 
	private User user;
	
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
			String dbUrl = "jdbc:mysql://remotemysql.com:3306/Cwyx6vUQDe";					
			String username = "Cwyx6vUQDe";	
			String password = "J8hC6uAYxS";
			String query = "SELECT a.id, b.name AS method, a.phoneNumber, a.catalogId, c.value, c.price, c.providerId, \n" + 
					"d.name AS provider, d.image, a.voucherId, e.name AS voucher, e.deduction, e.maxDeduction, \n" + 
					"f.name AS status, a.createdAt, a.updatedAt FROM transaction a\n" + 
					"JOIN payment_method b ON a.methodId = b.typeId\n" + 
					"JOIN pulsa_catalog c ON a.catalogId = c.id\n" + 
					"JOIN provider d ON c.providerId = d.id\n" + 
					"JOIN voucher e ON a.voucherId = e.id\n" + 
					"JOIN transaction_status f ON a.statusId = f.id\n" + 
					"WHERE a.userId = ? AND a.id = ?";
			
			Assert.assertEquals("success", message);
			
			if(!jsonPath.get("data").toString().equals("[]"))
			{
				List<Map<String, String>> data = jsonPath.getList("data");
				
				try {
					con = DriverManager.getConnection(dbUrl,username,password);
					con.setAutoCommit(true);
					
					for (int i = 0; i < data.size(); i++) {  
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
						
						PreparedStatement psGetHistoryInProgress = con.prepareStatement(query);
						psGetHistoryInProgress.setInt(1, Integer.parseInt(user.getId()));
						psGetHistoryInProgress.setInt(2, Integer.parseInt(data.get(i).get("id")));
						ResultSet result = psGetHistoryInProgress.executeQuery();
						
						while(result.next())
						{
							Assert.assertEquals(result.getInt("id"), data.get(i).get("id"));
							Assert.assertEquals(result.getString("method"), data.get(i).get("method"));
							Assert.assertEquals(result.getString("phone"), data.get(i).get("phone"));
							Assert.assertEquals(result.getInt("catalogId"), data.get(i).get("catalog.id"));
							Assert.assertEquals(result.getInt("value"), data.get(i).get("catalog.value"));
							Assert.assertEquals(result.getInt("price"), data.get(i).get("catalog.price"));
							Assert.assertEquals(result.getInt("providerId"), data.get(i).get("provider.id"));
							Assert.assertEquals(result.getString("provider"), data.get(i).get("provider.name"));
							Assert.assertEquals(result.getString("image"), data.get(i).get("provider.image"));
							Assert.assertEquals(result.getInt("voucherId"), data.get(i).get("voucher.id"));
							Assert.assertEquals(result.getString("voucher"), data.get(i).get("voucher.name"));
							Assert.assertEquals(result.getInt("deduction"), data.get(i).get("voucher.deduction"));
							Assert.assertEquals(result.getInt("maxDeduction"), data.get(i).get("voucher.maxDeduction"));
							Assert.assertEquals(result.getString("status"), data.get(i).get("status"));
							Assert.assertEquals(result.getDate("createdAt"), data.get(i).get("createdAt"));
							Assert.assertEquals(result.getDate("updatedAt"), data.get(i).get("updatedAt"));
						}
						
					}
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
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