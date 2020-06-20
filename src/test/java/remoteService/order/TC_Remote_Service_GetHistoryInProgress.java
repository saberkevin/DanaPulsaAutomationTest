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
import model.Catalog;
import model.User;

public class TC_Remote_Service_GetHistoryInProgress extends TestBase {
	private User user = new User();
	private String[] phoneNumbers = new String[11];
	private Catalog catalog = new Catalog();
	private String testCase;
	private String userId;
	private String page;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_GetHistoryInProgress(String testCase, String userId, String page, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.page = page;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void getHistoryInProgressRemoteService(String userId, String page) {
		logger.info("Call Get Get History in Progress API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("page:" + page);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_GET_HISTORY_IN_PROGRESS);
		requestParams.put("message", "{\"userId\":" + userId + ",\"page\":" + page + "}");
		
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
			
			// initialize catalog - TELKOMSEL 15k
			catalog.setId(13);
			catalog.setProviderId(2);
			catalog.setValue(15000);
			catalog.setPrice(15000);
			
			// insert transaction into database
			if (testCase.equals("Valid user id and page (below 10 history)")) {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 4);
				phoneNumbers[0] = user.getUsername();				
			} else if (testCase.equals("Valid user id and page (more than 10 history)")) {
				for (int i = 0; i < 11; i++) {
					createTransaction(user.getId(), "08125216179" + Integer.toString(i), catalog.getId(), 4);
					phoneNumbers[i] = "08125216179" + Integer.toString(i);
				}
			}

			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testHistoryInProgress() {
		getHistoryInProgressRemoteService(userId, page);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testHistoryInProgress"})
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
			List<HashMap<Object, Object>> history = response.jsonPath().get();			
			Assert.assertTrue(history.size() <= 10, "maximum history per page is only 10");
			
			for (int i = 0; i < history.size(); i++) {
				Assert.assertNotNull(history.get(i).get("id"));

				if (history.size() > 1) {
					Assert.assertEquals(history.get(i).get("phoneNumber"), phoneNumbers[history.size() - i]);
				} else {
					if (page.equals("2")) Assert.assertEquals(history.get(i).get("phoneNumber"), phoneNumbers[0]);													
					else Assert.assertEquals(history.get(i).get("phoneNumber"), user.getUsername());						
				}
				
				Assert.assertEquals(String.valueOf(history.get(i).get("price")), Long.toString(catalog.getPrice()));
				Assert.assertNotNull(history.get(i).get("voucher"));
				Assert.assertEquals(history.get(i).get("status"), "WAITING");
				Assert.assertNotNull(history.get(i).get("createdAt"));
			}
		}
	}
	
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
			data = sqlExec(query, param, "member");
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
			query = "SELECT A.id, A.phoneNumber, A.createdAt, B.price, C.name "
					+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
					+ "LEFT JOIN transaction_status C on A.statusId = C.id "
					+ "WHERE A.userId = ? AND A.statusId IN (3,4) "
					+ "ORDER BY A.createdAt DESC LIMIT ?, 10";
			param.put("1", Long.parseLong(userId));
			param.put("2", (Integer.parseInt(page)-1) * 10);
			data = sqlExec(query, param, "order");

			List<HashMap<Object, Object>> history = response.jsonPath().get();
			int index = 0;

			if (data.size() == 0) Assert.assertTrue(false, "no history found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(Long.valueOf((Integer) history.get(index).get("id")), map.get("id"));
				Assert.assertEquals(history.get(index).get("phoneNumber"), map.get("phoneNumber"));
				Assert.assertEquals(Long.valueOf((Integer) history.get(index).get("price")), map.get("price"));
				Assert.assertEquals(history.get(index).get("status"), map.get("name"));
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
