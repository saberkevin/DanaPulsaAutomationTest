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
import model.User;

public class TC_Remote_Service_CreateTransaction extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String phoneNumber;
	private String catalogId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_CreateTransaction(String testCase, String userId, String phoneNumber, String catalogId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.phoneNumber = phoneNumber;
		this.catalogId = catalogId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void createTransactionRemoteService(String userId, String phoneNumber, String catalogId) {
		logger.info("Call Create Transaction API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("phone number:" + phoneNumber);
		logger.info("catalog id:" + catalogId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_CREATE_TRANSACTION);
		requestParams.put("message", "{\"userId\":" + userId + ",\"phoneNumber\":\"" + phoneNumber + "\",\"catalogId\":" + catalogId + "}");
		
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
		
		if (userId.equals("true")) {
			// initialize user
			user.setName(ConfigRemoteServiceOrder.USER_NAME);
			user.setEmail(ConfigRemoteServiceOrder.USER_EMAIL);
			user.setUsername(ConfigRemoteServiceOrder.USER_USERNAME);
			user.setPin(ConfigRemoteServiceOrder.USER_PIN);
			
			// insert user into database
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));			
			createBalance(user.getId(), 10000000);
			userId = Long.toString(user.getId());

			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testCreateTransaction() {
		createTransactionRemoteService(userId, phoneNumber, catalogId);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testCreateTransaction"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "you’ve already requested this exact order within the last 30 seconds, "
				+ "please try again later if you actually intended to do that";
		final String errorMessage3 = "catalog not found";
		final String errorMessage4 = "selected catalog is not available for this phone’s provider";
		final String errorMessage5 = "invalid phone number";
		final String errorMessage6 = "unknown phone number";
		final String errorMessage7 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else if (responseBody.contains(errorMessage3)) {
			// do some code
		} else if (responseBody.contains(errorMessage4)) {
			// do some code
		} else if (responseBody.contains(errorMessage5)) {
			// do some code
		} else if (responseBody.contains(errorMessage6)) {
			// do some code
		} else if (responseBody.contains(errorMessage7)) {
			// do some code
		} else {
			Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("id")));
			Assert.assertEquals(response.getBody().jsonPath().get("phoneNumber"), phoneNumber);
			Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("catalog.id")), catalogId);
			Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("catalog.provider.id")));
			Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.name"));
			Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.image"));
			Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("catalog.value")));
			Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("catalog.price")));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "you’ve already requested this exact order within the last 30 seconds, "
				+ "please try again later if you actually intended to do that";
		final String errorMessage3 = "catalog not found";
		final String errorMessage4 = "selected catalog is not available for this phone’s provider";
		final String errorMessage5 = "invalid phone number";
		final String errorMessage6 = "unknown phone number";
		final String errorMessage7 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");
			Assert.assertTrue(data.size() == 0);

			param = new LinkedHashMap<String, Object>();
			query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", Long.parseLong(userId));
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			// do some code
			break;			
		case errorMessage3:
			query = "SELECT * FROM pulsa_catalog WHERE id = ?";
			param.put("1", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);

			param = new LinkedHashMap<String, Object>();
			query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", Long.parseLong(userId));
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage4:
			String providerName = "";

			query = "SELECT name FROM provider WHERE id IN (SELECT providerId FROM provider_prefix WHERE prefix = ?)";
			param.put("1", phoneNumber.substring(1, 5));
			data = sqlExec(query, param, "ORDER");
			
			if (data.size() == 0) Assert.assertTrue(false,  "no provider found in database");			
			for (Map<String, Object> map : data)
				providerName = (String) map.get("name");
			
			param = new LinkedHashMap<String, Object>();
			query = "SELECT B.name FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id WHERE A.id = ?";
			param.put("1", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");

			if (data.size() == 0) Assert.assertTrue(false,  "no provider found in database");			
			for (Map<String, Object> map : data)
				Assert.assertNotEquals(map.get("name"), providerName);

			param = new LinkedHashMap<String, Object>();
			query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", Long.parseLong(userId));
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage5:
			query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", Long.parseLong(userId));
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage6:
			query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", Long.parseLong(userId));
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage7:			
			query = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", Long.parseLong(userId));
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;
		default:
			query = "SELECT COUNT(*) AS count FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
			param.put("1", Long.parseLong(userId));
			param.put("2", phoneNumber);
			param.put("3", Long.parseLong(catalogId));
			data = sqlExec(query, param, "ORDER");

			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data)
				Assert.assertEquals(map.get("count"), 1L);
			break;
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
