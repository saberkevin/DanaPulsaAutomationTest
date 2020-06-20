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

public class TC_Integration_CreateOrder extends TestBase {
	private User user = new User();
	private String testCase;
	private String phoneNumber;
	private String catalogId;
	private String result;
	
	public TC_Integration_CreateOrder(String testCase, String phoneNumber, String catalogId, String result) {
		this.testCase = testCase;
		this.phoneNumber = phoneNumber;
		this.catalogId = catalogId;
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
		
		// get catalog
		getCatalog(user.getSessionId(), user.getUsername().substring(0,5));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
	}
	
	@Test
	public void testCreateOrder() {	
		createOrder(user.getSessionId(), phoneNumber, catalogId);
		checkStatusCode("201");

		Assert.assertTrue(response.getBody().asString().contains(result));
		
		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "201");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "created");
	}
	
	@Test(dependsOnMethods = {"testCreateOrder"})
	public void checkData() {
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.id")));
		Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), phoneNumber);
		Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("data.catalog.id")), catalogId);
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.provider.id")));
		Assert.assertNotNull(response.getBody().jsonPath().get("data.catalog.provider.name"));
		Assert.assertNotNull(response.getBody().jsonPath().get("data.catalog.provider.image"));
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.value")));
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.price")));
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		query = "SELECT COUNT(*) AS count FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
		param.put("1", user.getId());
		param.put("2", phoneNumber);
		param.put("3", Long.parseLong(catalogId));
		data = sqlExec(query, param, "ORDER");

		if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
		for (Map<String, Object> map : data)
			Assert.assertEquals(map.get("count"), 1L);
	}
	
	@AfterClass
	public void afterClass() {
		deleteTransactionByUserId(user.getId());
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		deleteTransactionByPhoneNumber(phoneNumber);			
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
