package integrationtest.order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.User;

public class TC_Integration_GetAllCatalog extends TestBase {
	private User user = new User();
	private String testCase;
	private String phonePrefix;
	private String result;
	
	public TC_Integration_GetAllCatalog(String testCase, String phonePrefix, String result) {
		this.testCase = testCase;
		this.phonePrefix = phonePrefix;
		this.result = result;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		// initialize user
		user.setName(ConfigIntegrationTestOrder.USER_NAME);
		user.setEmail(ConfigIntegrationTestOrder.USER_EMAIL);
		user.setUsername(ConfigIntegrationTestOrder.USER_USERNAME);
		user.setPin(ConfigIntegrationTestOrder.USER_PIN);
		
		// delete if exist
		deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
		deleteUserIfExist(user.getEmail(), user.getUsername());
		
		// register new user
		register(user.getName(), user.getEmail(), user.getUsername(), Integer.toString(user.getPin()));
		checkStatusCode("201");
		
		// login to system
		login("62" + user.getUsername().substring(1));
		checkStatusCode("200");
		user.setId(response.getBody().jsonPath().getLong("data.id"));
		
		// verify pin login
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
	}
	
	@Test
	public void testGetAllCatalog() {
		getCatalog(user.getSessionId(), phonePrefix);
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));

		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
	}
	
	@Test(dependsOnMethods = {"testGetAllCatalog"})
	public void checkData() {
		Map<String, String> provider = response.getBody().jsonPath().getMap("data.provider");
				
		Assert.assertNotNull(provider.get("id"));
		Assert.assertNotNull(provider.get("name"));
		Assert.assertNotNull(provider.get("image"));
		
		List<Map<String, String>> catalog = response.getBody().jsonPath().getList("data.catalog");
		
		for (int i = 0; i < catalog.size(); i++) {
			Assert.assertNotNull(catalog.get(i).get("id"));
			Assert.assertNotNull(catalog.get(i).get("value"));
			Assert.assertNotNull(catalog.get(i).get("price"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		query = "SELECT A.*, B.id AS providerId, B.name AS providerName, B.image AS providerImage "
				+ "FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id "
				+ "LEFT JOIN provider_prefix C on B.id = C.providerId "
				+ "WHERE C.prefix = ?";
		param.put("1", phonePrefix.substring(1));
		data = sqlExec(query, param, "order");
		
		List<Map<String, Object>> catalog = response.getBody().jsonPath().getList("data.catalog");
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
	}
	
	@AfterClass
	public void afterClass() {
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
