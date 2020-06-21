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

public class TC_Remote_Service_Pay extends TestBase {
	private User user = new User();
	private User anotherUser = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String userId;
	private String transactionId;
	private String paymentMethodId;
	private String voucherId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_Pay(String testCase, String userId, String transactionId, String paymentMethodId, String voucherId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.transactionId = transactionId;
		this.paymentMethodId = paymentMethodId;
		this.voucherId = voucherId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void paymentRemoteService(String userId, String transactionId, String paymentMethodId, String voucherId) {
		logger.info("Call Payment API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("transaction id:" + transactionId);
		logger.info("payment method id:" + paymentMethodId);
		logger.info("voucher id:" + voucherId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_PAY);
		requestParams.put("message", "{\"userId\":" + userId + ",\"transactionId\":" + transactionId 
				+ ",\"methodId\":" + paymentMethodId + ",\"voucherId\":" + voucherId + "}");
		
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
			
			if (userId.equals("true"))
				userId = Long.toString(user.getId());				
			
			// insert balance into database
			if (testCase.equals("Not enough balance")) {
				createBalance(user.getId(), 1000);
			} else {
				createBalance(user.getId(), 10000000);
				user.setBalance(10000000);
			}
			
			// insert voucher into database			
			if (voucherId.equals("1") || voucherId.equals("7") ||voucherId.equals("16")) {
				createUserVoucher(user.getId(), Long.parseLong(voucherId), 2);
			} else if (voucherId.equals("2") || voucherId.equals("4")) {
				createUserVoucher(user.getId(), Long.parseLong(voucherId), 1);
			}
			
			// set flag
			isCreateUser = true;
		}
		
		if (transactionId.equals("true")) {	
			// initialize catalog - TELKOMSEL 75k
			catalog.setId(19);
			catalog.setProviderId(2);
			catalog.setValue(75000);
			catalog.setPrice(75000);
			
			// initialize provider - TELKOMSEL
			provider.setId(2);
			provider.setName("Telkomsel");
			provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");

			// insert transaction into database
			if (testCase.equals("Transaction already completed")) {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 1);
			} else if (testCase.equals("Transaction already canceled")) {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 5);
			} else if (testCase.equals("Transaction already expired")) {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 6);
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
			
			// initialize catalog - TELKOMSEL 15k
			catalog.setId(13);
			catalog.setProviderId(2);
			catalog.setValue(15000);
			catalog.setPrice(15000);
			
			// initialize provider - TELKOMSEL
			provider.setId(2);
			provider.setName("Telkomsel");
			provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");

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
	public void testPayment() {
		paymentRemoteService(userId, transactionId, paymentMethodId, voucherId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testPayment"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "unknown transaction";
		final String errorMessage3 = "not enough balance";
		final String errorMessage4 = "user not found";
		final String errorMessage5 = "unknown method";
		final String errorMessage6 = "insufficient purchase amount to use this voucher";
		final String errorMessage7 = "your voucher not found";
		final String errorMessage8 = "your voucher is not applicable with your number";
		final String errorMessage9 = "your voucher is not applicable with payment method";
		final String errorMessage10 = "voucher you want to redeem is either not found, has been used or already expired";
		final String errorMessage11 = "invalid request format";
		
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
		} else if (responseBody.contains(errorMessage8)) {
			// do some code
		} else if (responseBody.contains(errorMessage9)) {
			// do some code
		} else if (responseBody.contains(errorMessage10)) {
			// do some code
		} else if (responseBody.contains(errorMessage11)) {
			// do some code
		} else {
			Assert.assertNotNull(response.getBody().jsonPath().getLong("balance"));			
			Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.id"), transaction.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("transaction.method"), transaction.getPaymentMethodName());
			Assert.assertEquals(response.getBody().jsonPath().get("transaction.phoneNumber"), transaction.getPhoneNumber());
			Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.catalog.id"), transaction.getCatalogId());
			Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.catalog.provider.id"), provider.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("transaction.catalog.provider.name"), provider.getName());
			Assert.assertEquals(response.getBody().jsonPath().get("transaction.catalog.provider.image"), provider.getImage());
			Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.catalog.value"), catalog.getValue());
			Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.catalog.price"), catalog.getPrice());
			Assert.assertEquals(response.getBody().jsonPath().get("transaction.status"), "COMPLETED");
			Assert.assertNotNull(response.getBody().jsonPath().get("transaction.createdAt"));
			Assert.assertNotNull(response.getBody().jsonPath().get("transaction.updatedAt"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "unknown transaction";
		final String errorMessage3 = "not enough balance";
		final String errorMessage4 = "user not found";
		final String errorMessage5 = "unknown method";
		final String errorMessage6 = "insufficient purchase amount to use this voucher";
		final String errorMessage7 = "your voucher not found";
		final String errorMessage8 = "your voucher is not applicable with your number";
		final String errorMessage9 = "your voucher is not applicable with payment method";
		final String errorMessage10 = "voucher you want to redeem is either not found, has been used or already expired";
		final String errorMessage11 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			query = "SELECT * FROM transaction WHERE id = ? AND userId = ? AND statusId IN (3, 4)";
			param.put("1", Long.parseLong(transactionId));
			param.put("2", Long.parseLong(userId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;			
		case errorMessage3:
			// do some code
			break;
		case errorMessage4:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage5:
			query = "SELECT * FROM payment_method WHERE id = ?";
			param.put("1", Long.parseLong(paymentMethodId));
			data = sqlExec(query, param, "ORDER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage6:
			query = "SELECT * FROM voucher WHERE id = ?";
			param.put("1", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data)
				Assert.assertTrue(catalog.getPrice() < (int) map.get("minPurchase"));

			break;
		case errorMessage7:
			query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
					+ "WHERE A.id = ? AND A.voucherId = ? AND A.voucherStatusId != 1 AND B.isActive = 1";
			param.put("1", Long.parseLong(userId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage8:
			query = "SELECT * FROM voucher A LEFT JOIN voucher_provider B on A.id = B.voucherId WHERE A.id = ? AND B.providerId = ?";
			param.put("1", Long.parseLong(voucherId));
			param.put("2", provider.getId());
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage9:
			query = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ?";
			param.put("1", Long.parseLong(paymentMethodId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage10:
			query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
					+ "WHERE A.userId = ? AND A.voucherId = ? AND A.voucherStatusId != 1 AND B.isActive = 1";
			param.put("1", Long.parseLong(userId));
			param.put("2", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage11:
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
			data = sqlExec(query, param, "ORDER");

			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.id"), map.get("id"));
				Assert.assertEquals(response.getBody().jsonPath().getString("transaction.method"), map.get("paymentMethodName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("transaction.phoneNumber"), map.get("phoneNumber"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.catalog.provider.id"), map.get("providerId"));
				Assert.assertEquals(response.getBody().jsonPath().getString("transaction.catalog.provider.name"), map.get("providerName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("transaction.catalog.provider.image"), map.get("providerImage"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.catalog.value"), map.get("value"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.catalog.price"), map.get("price"));
				Assert.assertEquals(response.getBody().jsonPath().getString("transaction.status"), map.get("transactionStatus"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("transaction.createdAt"), map.get("createdAt"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("transaction.updatedAt"), map.get("updatedAt"));
			}
			
			param = new LinkedHashMap<String, Object>();
			query = "SELECT * FROM balance WHERE userId = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");
			
			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data) {
//				Assert.assertEquals(response.getBody().jsonPath().getLong("transaction.id"), map.get("balance"));
			}
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteUserVoucherByUserId(user.getId());
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
