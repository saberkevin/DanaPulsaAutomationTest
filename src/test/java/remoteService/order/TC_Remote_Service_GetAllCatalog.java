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

public class TC_Remote_Service_GetAllCatalog extends TestBase {
	private String testCase;
	private String phonePrefix;
	private String result;
	private String dataAMQP;
	private JsonPath responseData;
	
	public TC_Remote_Service_GetAllCatalog(String testCase, String phonePrefix, String result) {
		this.testCase = testCase;
		this.phonePrefix = phonePrefix;
		this.result = result;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
	}
	
	@Test
	public void testGetAllCatalog() {
		dataAMQP = callRP(orderAMQP, ConfigRemoteServiceOrder.QUEUE_GET_ALL_CATALOG, phonePrefix);
		responseData = new JsonPath(dataAMQP);
		logger.info("message = " + phonePrefix);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testGetAllCatalog"})
	public void checkData() throws ParseException {
		Assert.assertTrue(dataAMQP.contains(result), dataAMQP);

		final String errorMessage1 = "unknown phone number";
		final String errorMessage2 = "invalid phone number";
		final String errorMessage3 = "invalid request format";
		
		if (dataAMQP.contains(errorMessage1)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage2)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage3)) {
			// do some code
		} else {
			Assert.assertNotNull(responseData.get("provider.id"));
			Assert.assertNotNull(responseData.get("provider.name"));
			Assert.assertNotNull(responseData.get("provider.image"));
			
			List<Map<String, String>> catalog = responseData.getList("catalog");
			
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
		
		switch (dataAMQP) {
		case errorMessage1:
			query = "SELECT * FROM provider_prefix WHERE prefix = ?";
			param.put("1", phonePrefix.substring(1));
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
			
			List<Map<String, Object>> catalog = responseData.getList("catalog");
			int index = 0;
			
			if (data.size() == 0) Assert.assertTrue(false, "no catalog found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(responseData.getLong("provider.id"), map.get("providerId"));
				Assert.assertEquals(responseData.getString("provider.name"), map.get("providerName"));
				Assert.assertEquals(responseData.getString("provider.image"), map.get("providerImage"));
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
