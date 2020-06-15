package remoteService.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;

public class TC_Remote_Service_GetAllCatalog extends TestBase {
	private String testCase;
	private String phonePrefix;
	private String result;
	
	public TC_Remote_Service_GetAllCatalog() {
		
	}
	
	public TC_Remote_Service_GetAllCatalog(String testCase, String phonePrefix, String result) {
		this.testCase = testCase;
		this.phonePrefix = phonePrefix;
		this.result = result;
	}

	@SuppressWarnings("unchecked")
	public void getAllCatalogRemoteService(String phonePrefix) {
		logger.info("Call Get All Catalog API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("phone prefix:" + phonePrefix);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", "getAllCatalog");
		requestParams.put("message", phonePrefix);
		
		RestAssured.baseURI = URIOrder;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.POST, "/api/test/");
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
	}
	
	@Test
	public void testGetAllCatalog() {
		getAllCatalogRemoteService(phonePrefix);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testGetAllCatalog"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();		
		Assert.assertTrue(responseBody.contains(result));
		
		if (!responseBody.equals("unknown phone number") 
				&& !responseBody.equals("invalid phone number") 
				&& !responseBody.equals("invalid request format")) {
			Assert.assertNotNull(response.getBody().jsonPath().get("provider.id"));
			Assert.assertNotNull(response.getBody().jsonPath().get("provider.name"));
			Assert.assertNotNull(response.getBody().jsonPath().get("provider.image"));
			
			List<Map<String, String>> catalog = response.getBody().jsonPath().getList("catalog");	
			for (int i = 0; i < catalog.size(); i++) {
				Assert.assertNotNull(catalog.get(i).get("id"));
				Assert.assertNotNull(catalog.get(i).get("value"));
				Assert.assertNotNull(catalog.get(i).get("price"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("unknown phone number")) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM provider_prefix WHERE prefix = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setString(1, phonePrefix);
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("invalid phone number")) {
			// do some code
			
		} else if (responseBody.equals("invalid request format")) {
			// do some code
			
		} else {
			List<Map<String, String>> catalog = response.getBody().jsonPath().getList("catalog");	

			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT "
						+ "A.*, "
						+ "B.id AS providerId, "
						+ "B.name AS providerName, "
						+ "B.image AS providerImage "
						+ "FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id "
						+ "LEFT JOIN provider_prefix C on B.id = C.providerId "
						+ "WHERE C.prefix = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setString(1, phonePrefix.substring(1));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no catalog found in database");
				}
				do {
					Assert.assertEquals(response.getBody().jsonPath().getLong("provider.id"), rs.getLong("providerId"));
					Assert.assertEquals(response.getBody().jsonPath().getString("provider.name"), rs.getString("providerName"));
					Assert.assertEquals(response.getBody().jsonPath().getString("provider.image"), rs.getString("providerImage"));
					Assert.assertEquals(String.valueOf(catalog.get(rs.getRow()-1).get("id")), rs.getString("id"));
					Assert.assertEquals(String.valueOf(catalog.get(rs.getRow()-1).get("value")), rs.getString("value"));
					Assert.assertEquals(String.valueOf(catalog.get(rs.getRow()-1).get("price")), rs.getString("price"));
				} while(rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
