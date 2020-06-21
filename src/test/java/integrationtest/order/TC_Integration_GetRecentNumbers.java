package integrationtest.order;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.User;

public class TC_Integration_GetRecentNumbers extends TestBase {
	private User user = new User();
	private long catalogId;
	private String[] phoneNumbers = new String[11];
	private String testCase;
	private String result;
	
	public TC_Integration_GetRecentNumbers(String testCase, String result) {
		this.testCase = testCase;
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
		
		// get catalog TELKOMSEL 30K
		getCatalog(user.getSessionId(), user.getUsername().substring(0,5));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		List<Map<String, Object>> vouchers = response.getBody().jsonPath().getList("data.catalog");
		catalogId = Long.valueOf((Integer) vouchers.get(1).get("id"));
		
		// create transaction
		if (testCase.equals("Valid ID (below 10 transaction history)")) {
			createOrder(user.getSessionId(), user.getUsername(), Long.toString(catalogId));
		} else if (testCase.equals("Valid ID (more than 10 transaction history)")) {
			for (int i = 0; i < 11; i++) {
				phoneNumbers[i] = "08125216179" + Integer.toString(i);
				createOrder(user.getSessionId(), phoneNumbers[i], Long.toString(catalogId));
			}
		}
	}
	
	@Test
	public void testRecentPhoneNumber() {
		getRecentPhoneNumber(user.getSessionId());
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"testRecentPhoneNumber"})
	public void checkData() throws ParseException {
		if (!response.getBody().jsonPath().getString("data").equals("[]")) {
			List<HashMap<Object, Object>> recentNumbers = response.jsonPath().getList("data");				
			Assert.assertTrue(recentNumbers.size() <= 10, "maximum recent number is only 10");
			
			for (int i = 0; i < recentNumbers.size(); i++) {if (recentNumbers.size() > 1)
				Assert.assertEquals(recentNumbers.get(i).get("number"), phoneNumbers[recentNumbers.size() - i]);
			else
				Assert.assertEquals(recentNumbers.get(i).get("number"), user.getUsername());					

				HashMap<String, String> provHashMap = (HashMap<String, String>) recentNumbers.get(i).get("provider");
				Assert.assertNotNull(String.valueOf(provHashMap.get("id")));
				Assert.assertNotNull(provHashMap.get("name"));
				Assert.assertNotNull(provHashMap.get("image"));
				
				Assert.assertNotNull(recentNumbers.get(i).get("date"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	@SuppressWarnings("unchecked")
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		if (response.getBody().jsonPath().getString("data").equals("[]")) {
			query = "SELECT * FROM transaction WHERE userId = ?";
			param.put("1", user.getId());
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
		} else {				
			query =  "SELECT A.phoneNumber, A.createdAt, C.* "
					+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
					+ "LEFT JOIN provider C on B.providerId = C.id "
					+ "WHERE A.userId = ? "
					+ "ORDER BY A.createdAt DESC LIMIT 10";
			param.put("1", user.getId());
			data = sqlExec(query, param, "ORDER");

			List<HashMap<Object, Object>> recentNumbers = response.jsonPath().getList("data");
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
		}
	}
	
	@AfterClass
	public void afterClass() {
		deleteTransactionByUserId(user.getId());
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
