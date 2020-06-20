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
import model.Catalog;
import model.Provider;
import model.Transaction;
import model.User;

public class TC_Remote_Service_Cancel extends TestBase {
	private User user = new User();
	private User anotherUser = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String userId;
	private String transactionId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_Cancel(String testCase, String userId, String transactionId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.transactionId = transactionId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void cancelTransactionRemoteService(String userId, String transactionId) {
		logger.info("Call Cancel API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("transaction id:" + transactionId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_CANCEL);
		requestParams.put("message", "{\"userId\":" + userId + ",\"transactionId\":" + transactionId + "}");
		
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
		
		// initialize catalog - TELKOMSEL 15k
		catalog.setId(13);
		catalog.setProviderId(2);
		catalog.setValue(15000);
		catalog.setPrice(15000);
		
		// initialize provider - TELKOMSEL
		provider.setId(2);
		provider.setName("Telkomsel");
		provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
		
		if (userId.equals("true") || transactionId.equals("true")) {
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
			if (userId.equals("true")) userId = Long.toString(user.getId());

			// set flag
			isCreateUser = true;
		}
		
		if (transactionId.equals("true")) {
			// insert transaction into database
			if (testCase.equals("Transaction already completed")) 
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 1);
			else 
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 4);
			
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
			createBalance(anotherUser.getId(), 10000000);
			anotherUser.setId(getUserIdByUsername(anotherUser.getUsername()));

			// insert transaction into database
			createTransaction(anotherUser.getId(), anotherUser.getUsername(), catalog.getId(), 4);
			transaction.setId(getTransactionIdByUserId(anotherUser.getId()));
			transactionId = Long.toString(transaction.getId());
		}
	}
	
	@Test
	public void testCancel() {
		cancelTransactionRemoteService(userId, transactionId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testCancel"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "unknown transaction";
		final String errorMessage3 = "can't cancel completed transaction";
		final String errorMessage4 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else if (responseBody.contains(errorMessage3)) {
			// do some code
		} else if (responseBody.contains(errorMessage4)) {
			// do some code
		} else {
			Assert.assertEquals(response.getBody().jsonPath().getLong("id"), transaction.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("method"), transaction.getPaymentMethodName());
			Assert.assertEquals(response.getBody().jsonPath().get("phoneNumber"), transaction.getPhoneNumber());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.id"), transaction.getCatalogId());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.provider.id"), provider.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("catalog.provider.name"), provider.getName());
			Assert.assertEquals(response.getBody().jsonPath().get("catalog.provider.image"), provider.getImage());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.value"), catalog.getValue());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.price"), catalog.getPrice());
			Assert.assertEquals(response.getBody().jsonPath().get("status"), "CANCELED");
			Assert.assertNotNull(response.getBody().jsonPath().get("createdAt"));
			Assert.assertNotNull(response.getBody().jsonPath().get("updatedAt"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "unknown transaction";
		final String errorMessage3 = "can't cancel completed transaction";
		final String errorMessage4 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "member");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			query = "SELECT * FROM transaction WHERE id = ? AND userId = ?";
			param.put("1", Long.parseLong(transactionId));
			param.put("2", Long.parseLong(userId));
			data = sqlExec(query, param, "order");
			Assert.assertTrue(data.size() == 0);			
			break;			
		case errorMessage3:
			query = "SELECT * FROM transaction WHERE id = ? AND userId = ?";
			param.put("1", Long.parseLong(transactionId));
			param.put("2", Long.parseLong(userId));
			data = sqlExec(query, param, "order");

			if (data.size() == 0) Assert.assertTrue(false,  "no transaction found in database");			
			for (Map<String, Object> map : data)
				Assert.assertEquals(1, map.get("statusId"));	

			break;
		case errorMessage4:
			// do some code
			break;
		default:
			query = "SELECT A.*, B.value, B.price, C.id AS providerId, C.name AS providerName, C.image AS providerImage, "
					+ "D.name AS transactionStatus, E.name AS paymentMethodName "
					+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
					+ "LEFT JOIN provider C on B.providerId = C.id "
					+ "LEFT JOIN transaction_status D on A.statusId = D.id "
					+ "LEFT JOIN payment_method E on A.methodId = E.id "
					+ "WHERE A.id = ? AND A.userId = ?";
			param.put("1", Long.parseLong(transactionId));
			param.put("2", Long.parseLong(userId));
			data = sqlExec(query, param, "order");

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("id"), map.get("id"));
				Assert.assertEquals(response.getBody().jsonPath().getString("method"), map.get("paymentMethodName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("phoneNumber"), map.get("phoneNumber"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.provider.id"), map.get("providerId"));
				Assert.assertEquals(response.getBody().jsonPath().getString("catalog.provider.name"), map.get("providerName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("catalog.provider.image"), map.get("providerImage"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.value"), map.get("value"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.price"), map.get("price"));
				Assert.assertEquals(response.getBody().jsonPath().getString("status"), map.get("transactionStatus"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("createdAt"), map.get("createdAt"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("updatedAt"), map.get("updatedAt"));
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
		
		if (testCase.equals("Another user's transaction")) {
			deleteTransactionByUserId(anotherUser.getId());
			deleteBalanceByUserId(anotherUser.getId());
			deleteUserByEmailAndUsername(anotherUser.getEmail(), anotherUser.getUsername());			
		}

		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
