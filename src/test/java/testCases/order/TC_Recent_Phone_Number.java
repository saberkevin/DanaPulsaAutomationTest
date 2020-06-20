package testCases.order;

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
import model.Provider;
import model.User;

public class TC_Recent_Phone_Number extends TestBase {
	private User user = new User();
	private String[] phoneNumbers = new String[11];
	private Provider provider = new Provider();
	private String testCase;
	private String sessionId;
	private String result;
	private boolean isCreateUser;

	public TC_Recent_Phone_Number(String testCase, String sessionId, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
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
	public void testRecentPhoneNumber() {
		getRecentPhoneNumber(sessionId);
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();		
		if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertTrue(response.getBody().asString().contains("Not Found") );
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"testRecentPhoneNumber"})
	public void checkData() throws ParseException {
		if (response.getStatusCode() == 200) {
			if (!response.getBody().jsonPath().getString("data").equals("[]")) {
				List<HashMap<Object, Object>> recentNumbers = response.jsonPath().getList("data");				
				Assert.assertTrue(recentNumbers.size() <= 10, "maximum recent number is only 10");
				
				for (int i = 0; i < recentNumbers.size(); i++) {
					if (recentNumbers.size() > 1)
						Assert.assertEquals(recentNumbers.get(i).get("number"), phoneNumbers[recentNumbers.size() - i]);
					else
						Assert.assertEquals(recentNumbers.get(i).get("number"), user.getUsername());						

					HashMap<String, String> provHashMap = (HashMap<String, String>) recentNumbers.get(i).get("provider");
					Assert.assertEquals(String.valueOf(provHashMap.get("id")), Long.toString(provider.getId()));
					Assert.assertEquals(provHashMap.get("name"), provider.getName());
					Assert.assertEquals(provHashMap.get("image"), provider.getImage());
					
					Assert.assertNotNull(recentNumbers.get(i).get("date"));
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		int statusCode = response.getStatusCode();
		if (statusCode == 200) {
			if (response.getBody().jsonPath().getString("data").equals("[]")) {
				query = "SELECT * FROM transaction WHERE userId = ?";
				param.put("1", user.getId());
				data = sqlExec(query, param, "order");
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
