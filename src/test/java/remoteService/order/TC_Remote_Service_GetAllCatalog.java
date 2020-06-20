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

public class TC_Remote_Service_GetAllCatalog extends TestBase {
	private String testCase;
	private String phonePrefix;
	private String result;
	
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
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_GET_ALL_CATALOG);
		requestParams.put("message", phonePrefix);
		
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
		Assert.assertTrue(responseBody.contains(result), responseBody);

		final String errorMessage1 = "unknown phone number";
		final String errorMessage2 = "invalid phone number";
		final String errorMessage3 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else if (responseBody.contains(errorMessage3)) {
			// do some code
		} else {
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
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";

		final String errorMessage1 = "unknown phone number";
		final String errorMessage2 = "invalid phone number";
		final String errorMessage3 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM provider_prefix WHERE prefix = ?";
			param.put("1", phonePrefix);
			data = sqlExec(query, param, "order");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			// do some code
			break;
		case errorMessage3:
			// do some code
			break;
		default:
			query = "SELECT A.*, B.id AS providerId, B.name AS providerName, B.image AS providerImage "
					+ "FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id "
					+ "LEFT JOIN provider_prefix C on B.id = C.providerId "
					+ "WHERE C.prefix = ?";
			param.put("1", phonePrefix.substring(1));
			data = sqlExec(query, param, "order");
			
			List<Map<String, Object>> catalog = response.getBody().jsonPath().getList("catalog");
			int index = 0;
			
			if (data.size() == 0) Assert.assertTrue(false, "no catalog found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("provider.id"), map.get("providerId"));
				Assert.assertEquals(response.getBody().jsonPath().getString("provider.name"), map.get("providerName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("provider.image"), map.get("providerImage"));
				Assert.assertEquals(Long.valueOf((Integer) catalog.get(index).get("id")), map.get("id"));
				Assert.assertEquals(Long.valueOf((Integer) catalog.get(index).get("value")), map.get("value"));
				Assert.assertEquals(Long.valueOf((Integer) catalog.get(index).get("price")), map.get("price"));
				index++;
			}
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
