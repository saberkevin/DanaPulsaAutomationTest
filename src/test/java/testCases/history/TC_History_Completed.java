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

public class TC_History_Completed extends TestBase{
	
	private String id; 
	private User user;
	
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
			String dbUrl = "jdbc:mysql://remotemysql.com:3306/Cwyx6vUQDe";					
			String username = "Cwyx6vUQDe";	
			String password = "J8hC6uAYxS";
			String query = "SELECT a.id,a.userId,a.phoneNumber,d.price,c.name as voucher,b.name AS status,a.createdAt FROM transaction a\n" + 
					"JOIN transaction_status b ON a.statusId = b.id AND b.typeId = 2\n" + 
					"JOIN voucher c ON a.voucherId = c.id \n" + 
					"JOIN pulsa_catalog d ON a.catalogId = d.id \n" +
					"WHERE a.userId = ? and a.id = ? \n" + 
					"ORDER BY a.createdAt";
			
			Assert.assertEquals("success", message);
			
			if(!jsonPath.get("data").toString().equals("[]"))
			{
				List<Map<String, String>> data = jsonPath.getList("data");
				
				try {
					con = DriverManager.getConnection(dbUrl,username,password);
					con.setAutoCommit(true);
					
					for (int i = 0; i < data.size(); i++) {  
						Assert.assertNotEquals("", data.get(i).get("id"));
						Assert.assertNotEquals("", data.get(i).get("phone"));
						Assert.assertNotEquals("", data.get(i).get("price"));
						Assert.assertNotEquals("", data.get(i).get("voucher"));
						Assert.assertNotEquals("", data.get(i).get("createdAt"));
						Assert.assertTrue(data.get(i).get("status").equals("COMPLETED") || 
								data.get(i).get("status").equals("CANCELED") ||
								data.get(i).get("status").equals("EXPIRED") ||
								data.get(i).get("status").equals("FAILED") 
						);
						
						PreparedStatement psGetHistoryInProgress = con.prepareStatement(query);
						psGetHistoryInProgress.setInt(1, Integer.parseInt(user.getId()));
						psGetHistoryInProgress.setInt(2, Integer.parseInt(data.get(i).get("id")));
						ResultSet result = psGetHistoryInProgress.executeQuery();
						
						while(result.next())
						{
							Assert.assertEquals(result.getInt("id"), data.get(i).get("id"));
							Assert.assertEquals(result.getString("phone"), data.get(i).get("phone"));
							Assert.assertEquals(result.getInt("price"), data.get(i).get("price"));
							Assert.assertEquals(result.getString("voucher"), data.get(i).get("voucher"));
							Assert.assertEquals(result.getString("status"), data.get(i).get("status"));
							Assert.assertEquals(result.getDate("createdAt"), data.get(i).get("createdAt"));
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