package remoteService.order;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Remote_Service_GetProviderById extends TestBase {
	private String testCase;
	private String providerId;
	private String result;
	private String dataAMQP;
	private JsonPath responseData;

	public TC_Remote_Service_GetProviderById(String testCase, String providerId, String result) {
		this.testCase = testCase;
		this.providerId = providerId;
		this.result = result;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
	}
	
	@Test
	public void testGetProviderById() {
		dataAMQP = callRP(orderAMQP, ConfigRemoteServiceOrder.QUEUE_GET_PROVIDER_BY_ID, providerId);
		responseData = new JsonPath(dataAMQP);
		logger.info("message = " + providerId);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testGetProviderById"})
	public void checkData() throws ParseException {
		Assert.assertTrue(dataAMQP.contains(result), dataAMQP);

		final String errorMessage1 = "unknown provider";
		final String errorMessage2 = "invalid request format";
		
		if (dataAMQP.contains(errorMessage1)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage2)) {
			// do some code
		} else {
			Assert.assertNotNull(responseData.get("id"));
			Assert.assertNotNull(responseData.get("name"));
			Assert.assertNotNull(responseData.get("image"));			
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown provider";
		final String errorMessage2 = "invalid request format";
		
		switch (dataAMQP) {
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
				Assert.assertEquals(responseData.getLong("id"), map.get("id"));
				Assert.assertEquals(responseData.getString("name"), map.get("name"));
				Assert.assertEquals(responseData.getString("image"), map.get("image"));
			}
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
