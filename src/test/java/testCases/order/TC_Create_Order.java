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

public class TC_Create_Order extends TestBase {
	private User user = new User();
	private String testCase;
	private String sessionId;
	private String phoneNumber;
	private String catalogId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Create_Order(String testCase, String sessionId, String phoneNumber, String catalogId, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.phoneNumber = phoneNumber;
		this.catalogId = catalogId;
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
	public void testCreateOrder() {	
		createOrder(sessionId, phoneNumber, catalogId);
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();
		if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertTrue(response.getBody().jsonPath().getString("message").equals("invalid phone number")
					|| response.getBody().jsonPath().getString("message").equals("selected catalog is not available for this phone’s provider")
					|| response.getBody().jsonPath().getString("message").contains("phone number must not be null")
					|| response.getBody().jsonPath().getString("message").contains("catalog ID must not be null"));
		} else if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "404");
			Assert.assertTrue(response.getBody().jsonPath().getString("message").equals("catalog not found")
					|| response.getBody().jsonPath().getString("message").equals("unknown phone number"));
		} else if (statusCode == 409) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "409");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "you’ve already requested this exact order within the last 30 seconds, "
					+ "please try again later if you actually intended to do that");
		} else if (statusCode == 201) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "201");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "created");
		}
	}
	
	@Test(dependsOnMethods = {"testCreateOrder"})
	public void checkData() {
		if (response.getStatusCode() == 201) {
			Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.id")));
			Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), phoneNumber);
			Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("data.catalog.id")), catalogId);
			
			Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("data.catalog.provider.id")), "2");
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.name"), "Telkomsel");
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.image"), 
					"https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
			
			Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("data.catalog.value")), "15000");
			Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("data.catalog.price")), "15000");
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		int statusCode = response.getStatusCode();		
		if (statusCode == 400) {
			if (response.getBody().asString().contains("selected catalog is not available for this phone’s provider")) {
				String providerName = "";

				query = "SELECT name FROM provider WHERE id IN (SELECT providerId FROM provider_prefix WHERE prefix = ?)";
				param.put("1", phoneNumber.substring(1, 5));
				data = sqlExec(query, param, "ORDER");
				
				if (data.size() == 0) Assert.assertTrue(false,  "no provider found in database");			
				for (Map<String, Object> map : data)
					providerName = (String) map.get("name");
				
				query = "SELECT B.name FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id WHERE A.id = ?";
				param.put("1", Long.parseLong(catalogId));
				data = sqlExec(query, param, "ORDER");

				if (data.size() == 0) Assert.assertTrue(false,  "no provider found in database");			
				for (Map<String, Object> map : data)
					Assert.assertNotEquals(map.get("name"), providerName);

				query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				param.put("1", user.getId());
				param.put("2", phoneNumber);
				param.put("3", Long.parseLong(catalogId));
				data = sqlExec(query, param, "ORDER");
				Assert.assertTrue(data.size() == 0);
			} else if (response.getBody().asString().contains("invalid phone number")) {
				// do some code
				
			}			
		} else if (statusCode == 404) {
			if (response.getBody().asString().contains("catalog not found")) {
				query = "SELECT * FROM pulsa_catalog WHERE id = ?";
				param.put("1", Long.parseLong(catalogId));
				data = sqlExec(query, param, "ORDER");
				Assert.assertTrue(data.size() == 0);

				query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				param.put("1", user.getId());
				param.put("2", phoneNumber);
				param.put("3", Long.parseLong(catalogId));
				data = sqlExec(query, param, "ORDER");
				Assert.assertTrue(data.size() == 0);
			} else if (response.getBody().asString().contains("unknown phone number")) {
				query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				param.put("1", user.getId());
				param.put("2", phoneNumber);
				param.put("3", Long.parseLong(catalogId));
				data = sqlExec(query, param, "ORDER");
				Assert.assertTrue(data.size() == 0);
			}
		} else if (statusCode == 409) {
			// do some code
			
		} else if (statusCode == 201) {
			query = "SELECT COUNT(*) AS count FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", user.getId());
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");

			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data)
				Assert.assertEquals(map.get("count"), 1L);
		}
	}
	
	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteTransactionByUserId(user.getId());
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		deleteTransactionByPhoneNumber(phoneNumber);			
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
