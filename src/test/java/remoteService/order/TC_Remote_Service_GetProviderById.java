package remoteService.order;

import java.text.ParseException;
import java.util.ArrayList;
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
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_GET_PROVIDER_BY_ID);
		requestParams.put("message", providerId);
		
		RestAssured.baseURI = ConfigRemoteServiceOrder.BASE_URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.POST, ConfigRemoteServiceOrder.ENDPOINT_PATH);
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

		final String errorMessage1 = "unknown provider";
		final String errorMessage2 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else {
			Assert.assertNotNull(response.getBody().jsonPath().get("id"));
			Assert.assertNotNull(response.getBody().jsonPath().get("name"));
			Assert.assertNotNull(response.getBody().jsonPath().get("image"));			
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown provider";
		final String errorMessage2 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM provider WHERE id = ?";
			param.put("1", Long.parseLong(providerId));
			data = sqlExec(query, param, "order");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			// do some code
			break;
		default:
			query = "SELECT * FROM provider WHERE id = ?";
			param.put("1", Long.parseLong(providerId));
			data = sqlExec(query, param, "order");
			
			if (data.size() == 0) Assert.assertTrue(false, "no provider found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("id"), map.get("id"));
				Assert.assertEquals(response.getBody().jsonPath().getString("name"), map.get("name"));
				Assert.assertEquals(response.getBody().jsonPath().getString("image"), map.get("image"));
			}
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
