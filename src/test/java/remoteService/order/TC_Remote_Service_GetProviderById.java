package remoteService.order;

import java.text.ParseException;
import java.util.LinkedHashMap;
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

public class TC_Remote_Service_GetProviderById extends TestBase {
	private String testCase;
	private String providerId;
	private String result;
	
	public TC_Remote_Service_GetProviderById() {
		
	}

	public TC_Remote_Service_GetProviderById(String testCase, String providerId, String result) {
		this.testCase = testCase;
		this.providerId = providerId;
		this.result = result;
	}
	
	@SuppressWarnings("unchecked")
	public void getProviderByIdRemoteService(String providerId) {
		logger.info("Call Get Provider By Id API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("provider id:" + providerId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", "getProviderById");
		requestParams.put("message", providerId);
		
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
	public void testGetProviderById() {
		getProviderByIdRemoteService(providerId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testGetProviderById"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.equals("unknown provider") && !responseBody.equals("invalid request format")) {
			Assert.assertNotNull(response.getBody().jsonPath().get("id"));
			Assert.assertNotNull(response.getBody().jsonPath().get("name"));
			Assert.assertNotNull(response.getBody().jsonPath().get("image"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("unknown provider")) {
			String queryString = "SELECT * FROM provider WHERE id = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(providerId));
			
			List<Map<String, Object>> data = sqlExec(queryString, param, "order");			
			Assert.assertTrue(data.size() == 0, "no provider found in database");
			
		} else if (responseBody.equals("invalid request format")) {
			// do some code
			
		} else {
			String queryString = "SELECT * FROM provider WHERE id = ?";
			
			Map<String, Object> param = new LinkedHashMap<String, Object>();
			param.put("id", Long.parseLong(providerId));
			
			List<Map<String, Object>> data = sqlExec(queryString, param, "order");
			
			if (data.size() == 0) {
				Assert.assertTrue(false, "no provider found in database");
			}
			
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("id"), map.get("id"));
				Assert.assertEquals(response.getBody().jsonPath().getString("name"), map.get("name"));
				Assert.assertEquals(response.getBody().jsonPath().getString("image"), map.get("image"));
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
