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
import model.Catalog;
import model.Provider;
import model.Transaction;
import model.User;

public class TC_Cancel_Order extends TestBase {
	private User user = new User();
	private User anotherUser = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String sessionId;
	private String transactionId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Cancel_Order(String testCase, String sessionId, String transactionId, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.transactionId = transactionId;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		// initialize catalog - TELKOMSEL 15k
		catalog.setId(13);
		catalog.setProviderId(2);
		catalog.setValue(15000);
		catalog.setPrice(15000);
		
		// initialize provider - TELKOMSEL
		provider.setId(2);
		provider.setName("Telkomsel");
		provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
		
		if (sessionId.equals("true") || transactionId.equals("true")) {
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
			
			if (sessionId.equals("true")) {				
				verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
				checkStatusCode("200");
				user.setSessionId(response.getCookie("JSESSIONID"));
				sessionId = user.getSessionId();		
			}
			
			// set flag
			isCreateUser = true;
		}
		
		if (transactionId.equals("true")) {	
			// insert transaction into database
			if (testCase.equals("Transaction already completed")) {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 1);
			} else {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 4);				
			}
			
			// initialize transaction
			transaction.setId(getTransactionIdByUserId(user.getId()));
			transaction.setPhoneNumber(user.getUsername());
			transaction.setCatalogId(catalog.getId());
			transaction.setMethodId(1);
			transaction.setPaymentMethodName("WALLET");			
			transactionId = Long.toString(transaction.getId());
		}
		
		if (testCase.equals("Another user's transaction")) {			
			// initialize user
			anotherUser.setName("Zanuar 2");
			anotherUser.setEmail("triromadon2@gmail.com");
			anotherUser.setUsername("081252930397");
			anotherUser.setPin(123456);
			
			// insert user into database
			deleteBalanceByEmailByUsername(anotherUser.getEmail(), anotherUser.getUsername());
			deleteUserIfExist(anotherUser.getEmail(), anotherUser.getUsername());			
			createUser(anotherUser);
			anotherUser.setId(getUserIdByUsername(anotherUser.getUsername()));
			createBalance(anotherUser.getId(), 10000000);
			
			// insert transaction into database
			createTransaction(anotherUser.getId(), anotherUser.getUsername(), catalog.getId(), 4);
			transaction.setId(getTransactionIdByUserId(anotherUser.getId()));
			transaction.setPhoneNumber(anotherUser.getUsername());
			transaction.setCatalogId(catalog.getId());
			transaction.setMethodId(1);
			transaction.setPaymentMethodName("WALLET");			
			transactionId = Long.toString(transaction.getId());
		}
	}
	
	@Test
	public void testCancelOrder() {
		cancelOrder(sessionId, transactionId);
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();		
		if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "can't cancel completed transaction");
		} else if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertTrue(response.getBody().asString().contains("Not Found") 
					|| response.getBody().asString().contains("unknown transaction"));
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "deleted");
		}
	}
	
	@Test(dependsOnMethods = {"testCancelOrder"})
	public void checkData() {
		if (response.getStatusCode() == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.id"), transaction.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("data.method"), transaction.getPaymentMethodName());
			Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), transaction.getPhoneNumber());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.id"), transaction.getCatalogId());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.provider.id"), provider.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.name"), provider.getName());
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.image"), provider.getImage());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.value"), catalog.getValue());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.price"), catalog.getPrice());
			Assert.assertEquals(response.getBody().jsonPath().get("data.status"), "CANCELED");
			Assert.assertNotNull(response.getBody().jsonPath().get("data.createdAt"));
			Assert.assertNotNull(response.getBody().jsonPath().get("data.updatedAt"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";

		int statusCode = response.getStatusCode();		
		if (statusCode == 400) {
			if (response.getBody().asString().equals("can't cancel completed transaction")) {
				query = "SELECT * FROM transaction WHERE id = ? AND userId = ?";
				param.put("1", Long.parseLong(transactionId));
				param.put("2", user.getId());
				data = sqlExec(query, param, "order");

				if (data.size() == 0) Assert.assertTrue(false,  "no transaction found in database");			
				for (Map<String, Object> map : data)
					Assert.assertEquals(1L, map.get("statusId"));	

			}
		} else if (statusCode == 404) {
			if (response.getBody().asString().equals("unknown transaction")) {
				query = "SELECT * FROM transaction WHERE id = ? AND userId = ?";
				param.put("1", Long.parseLong(transactionId));
				param.put("2", user.getId());
				data = sqlExec(query, param, "order");
				Assert.assertTrue(data.size() == 0);	
			}			
		} else if (statusCode == 200) {
			query = "SELECT A.*, B.value, B.price, C.id AS providerId, C.name AS providerName, C.image AS providerImage, "
					+ "D.name AS transactionStatus, E.name AS paymentMethodName "
					+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
					+ "LEFT JOIN provider C on B.providerId = C.id "
					+ "LEFT JOIN transaction_status D on A.statusId = D.id "
					+ "LEFT JOIN payment_method E on A.methodId = E.id "
					+ "WHERE A.id = ? AND A.userId = ?";
			param.put("1", Long.parseLong(transactionId));
			param.put("2", user.getId());
			data = sqlExec(query, param, "order");

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.id"), map.get("id"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.method"), map.get("paymentMethodName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.phoneNumber"), map.get("phoneNumber"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.provider.id"), map.get("providerId"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.catalog.provider.name"), map.get("providerName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.catalog.provider.image"), map.get("providerImage"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.value"), map.get("value"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.price"), map.get("price"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.status"), map.get("transactionStatus"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("data.createdAt"), map.get("createdAt"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("data.updatedAt"), map.get("updatedAt"));
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

		if (testCase.equals("Another user's transaction")) {
			deleteTransactionByUserId(anotherUser.getId());
			deleteBalanceByUserId(anotherUser.getId());
			deleteUserByEmailAndUsername(anotherUser.getEmail(), anotherUser.getUsername());			
		}

		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
