package remoteService.order;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
import model.Provider;
import model.User;

public class TC_Remote_Service_GetRecentNumber extends TestBase {
	private User user = new User();
	private String[] phoneNumbers = new String[11];
	private Provider provider = new Provider();
	private String testCase;
	private String userId;
	private String result;
	private boolean isCreateUser;

	public TC_Remote_Service_GetRecentNumber(String testCase, String userId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void getRecentNumberRemoteService(String userId) {
		logger.info("Call Get Recent Number API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_GET_RECENT_NUMBER);
		requestParams.put("message", userId);
		
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
						
			// initialize provider - TELKOMSEL
			provider.setId(2);
			provider.setName("Telkomsel");
			provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
			
			// insert transaction into database
			if (testCase.equals("Valid ID (below 10 transaction history)")) {
				createTransaction(user.getId(), user.getUsername(), 13);
				phoneNumbers[0] = user.getUsername();
			} else if (testCase.equals("Valid ID (more than 10 transaction history)")) {
				for (int i = 0; i < 11; i++) {
					phoneNumbers[i] = "08125216179" + Integer.toString(i);
					createTransaction(user.getId(), phoneNumbers[i], 13);
				}
			}
			
			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testRecentNumber() {
		getRecentNumberRemoteService(userId);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"testRecentNumber"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "[]";
		final String errorMessage3 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else if (responseBody.contains(errorMessage3)) {
			// do some code
		} else {
			List<HashMap<Object, Object>> recentNumbers = response.jsonPath().get();			
			Assert.assertTrue(recentNumbers.size() <= 10, "maximum recent number is only 10");
			
			for (int i = 0; i < recentNumbers.size(); i++) {
				if (recentNumbers.size() > 1) Assert.assertEquals(recentNumbers.get(i).get("number"), phoneNumbers[recentNumbers.size() - i]);
				else Assert.assertEquals(recentNumbers.get(i).get("number"), user.getUsername());						

				HashMap<String, String> provHashMap = (HashMap<String, String>) recentNumbers.get(i).get("provider");
				Assert.assertEquals(String.valueOf(provHashMap.get("id")), Long.toString(provider.getId()));
				Assert.assertEquals(provHashMap.get("name"), provider.getName());
				Assert.assertEquals(provHashMap.get("image"), provider.getImage());
				
				Assert.assertNotNull(recentNumbers.get(i).get("date"));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "[]";
		final String errorMessage3 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			query = "SELECT * FROM transaction WHERE userId = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "order");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage3:
			// do some code
			break;
		default:
			query =  "SELECT A.phoneNumber, A.createdAt, C.* "
					+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
					+ "LEFT JOIN provider C on B.providerId = C.id "
					+ "WHERE A.userId = ? "
					+ "ORDER BY A.createdAt DESC LIMIT 10";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "ORDER");

			List<HashMap<Object, Object>> recentNumbers = response.jsonPath().get();
			int index = 0;

			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(recentNumbers.get(index).get("number"), map.get("phoneNumber"));
				
				HashMap<String, Object> provHashMap = (HashMap<String, Object>) recentNumbers.get(index).get("provider");
				Assert.assertEquals(Long.valueOf((Integer) provHashMap.get("id")), map.get("id"));
				Assert.assertEquals(provHashMap.get("name"), map.get("name"));
				Assert.assertEquals(provHashMap.get("image"), map.get("image"));
				index++;
			}
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
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
