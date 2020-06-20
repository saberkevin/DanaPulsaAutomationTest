package testCases.order;

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

public class TC_Mobile_Recharge_Catalog extends TestBase {
	private User user = new User();
	private String testCase;
	private String sessionId;
	private String phonePrefix;
	private String result;
	private boolean isCreateUser;
	
	public TC_Mobile_Recharge_Catalog(String testCase, String sessionId, String phonePrefix, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.phonePrefix = phonePrefix;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (sessionId.equals("true")) {			
			// initialize user
			user.setName(ConfigApiTestOrder.USER_NAME);
			user.setEmail(ConfigApiTestOrder.USER_EMAIL);
			user.setUsername(ConfigApiTestOrder.USER_USERNAME);
			user.setPin(ConfigApiTestOrder.USER_PIN);
			
			// insert user into database
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());			
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));	
			createBalance(user.getId(), 10000000);
			
			// verify pin login
			verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
			checkStatusCode("200");
			user.setSessionId(response.getCookie("JSESSIONID"));
			sessionId = user.getSessionId();
			
			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testMobileRechargeCatalog() {
		getCatalog(sessionId, phonePrefix);
		
		Assert.assertTrue(response.getBody().asString().contains(result));
		user.setSessionId(response.getCookie("JSESSIONID"));

		int statusCode = response.getStatusCode();
		if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "invalid phone number");
		} else if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertTrue(response.getBody().asString().contains("Not Found") 
					|| response.getBody().asString().contains("unknown phone number"));
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
		}
	}
	
	@Test(dependsOnMethods = {"testMobileRechargeCatalog"})
	public void checkData() {
		if (response.getStatusCode() == 200) {
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
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		int statusCode = response.getStatusCode();		
		if (statusCode == 404) {
			if (response.getBody().asString().contains("unknown phone number")) {
				query = "SELECT * FROM provider_prefix WHERE prefix = ?";
				param.put("1", phonePrefix);
				data = sqlExec(query, param, "order");
				Assert.assertTrue(data.size() == 0);
			}
		} else if (statusCode == 400) {
			// do some code
			
		} else if (statusCode == 200) {
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
	}
	
	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
