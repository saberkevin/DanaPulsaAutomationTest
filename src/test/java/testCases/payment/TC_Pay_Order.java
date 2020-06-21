package testCases.payment;

import java.text.ParseException;
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

public class TC_Pay_Order extends TestBase {
	private User user = new User();
	private User anotherUser = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String sessionId;
	private String transactionId;
	private String paymentMethodId;
	private String voucherId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Pay_Order(String testCase, String sessionId, String transactionId, String paymentMethodId, String voucherId, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.transactionId = transactionId;
		this.paymentMethodId = paymentMethodId;
		this.voucherId = voucherId;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (sessionId.equals("true") || transactionId.equals("true")) {
			// initialize user
			user.setName(ConfigApiTestPayment.USER_NAME);
			user.setEmail(ConfigApiTestPayment.USER_EMAIL);
			user.setUsername(ConfigApiTestPayment.USER_USERNAME);
			user.setPin(ConfigApiTestPayment.USER_PIN);
			
			// insert user into database
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());			
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));
			
			if (sessionId.equals("true")) {				
				verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
				checkStatusCode("200");
				user.setSessionId(response.getCookie("JSESSIONID"));
				sessionId = user.getSessionId();			
			}
			
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
			} else if (voucherId.equals("3") || voucherId.equals("4")) {
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
	public void testPayOrder() {
		payOrder(sessionId, transactionId, paymentMethodId, voucherId);
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();
		if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertTrue(response.getBody().jsonPath().getString("message").equals("not enough balance")
					|| response.getBody().jsonPath().getString("message").equals("your voucher not found")
					|| response.getBody().jsonPath().getString("message").equals("your voucher is not applicable with your number")
					|| response.getBody().jsonPath().getString("message").equals("your voucher is not applicable with payment method")
					|| response.getBody().jsonPath().getString("message").equals("insufficient purchase amount to use this voucher"));
		} else if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "404");
			Assert.assertTrue(response.getBody().jsonPath().getString("message").equals("unknown transaction")
					|| response.getBody().jsonPath().getString("message").equals("unknown method"));
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
		}
	}
	
	@Test(dependsOnMethods = {"testPayOrder"})
	public void checkData() throws ParseException {
		if (response.getStatusCode() == 200) {
			Assert.assertNotNull(response.getBody().jsonPath().getLong("data.balance"));			
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.id"), transaction.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("data.transaction.method"), transaction.getPaymentMethodName());
			Assert.assertEquals(response.getBody().jsonPath().get("data.transaction.phoneNumber"), transaction.getPhoneNumber());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.catalog.id"), transaction.getCatalogId());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.catalog.provider.id"), provider.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("data.transaction.catalog.provider.name"), provider.getName());
			Assert.assertEquals(response.getBody().jsonPath().get("data.transaction.catalog.provider.image"), provider.getImage());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.catalog.value"), catalog.getValue());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.catalog.price"), catalog.getPrice());
			Assert.assertEquals(response.getBody().jsonPath().get("data.transaction.status"), "COMPLETED");
			Assert.assertNotNull(response.getBody().jsonPath().get("data.transaction.createdAt"));
			Assert.assertNotNull(response.getBody().jsonPath().get("data.transaction.updatedAt"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		int statusCode = response.getStatusCode();
		if (statusCode == 400) {
			if (response.getBody().asString().contains("not enough balance")) {
				int discount = 0;
				int maxDeduction = 0;
				
				if (!voucherId.equals("0")) {
					query = "SELECT * FROM voucher WHERE id = ?";
					param.put("1", Long.parseLong(voucherId));
					data = sqlExec(query, param, "PROMOTION");
					
					if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
					for (Map<String, Object> map : data) {
						discount = (int) map.get("discount");
						maxDeduction = (int) map.get("maxDeduction");
						
						if (discount * catalog.getPrice() > maxDeduction) discount = maxDeduction;
					}
				}
				
				Assert.assertTrue((int) user.getBalance() < (catalog.getPrice() - discount));
				
			} else if (response.getBody().asString().contains("your voucher not found")) {
				query = "SELECT * FROM user_voucher A LEFT JOIN voucher B ON A.voucherId = B.id "
						+ "WHERE A.id = ? AND A.voucherId = ? AND A.voucherStatusId != 1 AND B.isActive = 1";
				param.put("1", user.getId());
				param.put("2", Long.parseLong(voucherId));
				data = sqlExec(query, param, "PROMOTION");
				Assert.assertTrue(data.size() == 0);
			} else if (response.getBody().asString().contains("your voucher is not applicable with your number")) {
				query = "SELECT * FROM voucher A LEFT JOIN voucher_provider B on A.id = B.voucherId WHERE A.id = ? AND B.providerId = ?";
				param.put("1", Long.parseLong(voucherId));
				param.put("2", provider.getId());
				data = sqlExec(query, param, "PROMOTION");
				Assert.assertTrue(data.size() == 0);
			} else if (response.getBody().asString().contains("your voucher is not applicable with payment method")) {
				query = "SELECT * FROM voucher_payment_method WHERE paymentMethodId = ?";
				param.put("1", Long.parseLong(paymentMethodId));
				data = sqlExec(query, param, "PROMOTION");
				Assert.assertTrue(data.size() == 0);
			} else if (response.getBody().asString().contains("insufficient purchase amount to use this voucher")) {
				query = "SELECT * FROM voucher WHERE id = ?";
				param.put("1", Long.parseLong(voucherId));
				data = sqlExec(query, param, "PROMOTION");

				if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
				for (Map<String, Object> map : data)
					Assert.assertTrue(catalog.getPrice() < (int) map.get("minPurchase"));
			}
		} else if (statusCode == 404) {
			if (response.getBody().asString().contains("unknown transaction")) {
				query = "SELECT * FROM transaction WHERE id = ? AND userId = ? AND statusId IN (3, 4)";
				param.put("1", Long.parseLong(transactionId));
				param.put("1", user.getId());
				data = sqlExec(query, param, "ORDER");
				Assert.assertTrue(data.size() == 0);
			} else if (response.getBody().asString().contains("unknown method")) {
				query = "SELECT * FROM payment_method WHERE id = ?";
				param.put("1", Long.parseLong(paymentMethodId));
				data = sqlExec(query, param, "ORDER");
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
			data = sqlExec(query, param, "ORDER");

			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.id"), map.get("id"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.transaction.method"), map.get("paymentMethodName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.transaction.phoneNumber"), map.get("phoneNumber"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.catalog.provider.id"), map.get("providerId"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.transaction.catalog.provider.name"), map.get("providerName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.transaction.catalog.provider.image"), map.get("providerImage"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.catalog.value"), map.get("value"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("data.transaction.catalog.price"), map.get("price"));
				Assert.assertEquals(response.getBody().jsonPath().getString("data.transaction.status"), map.get("transactionStatus"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("data.transaction.createdAt"), map.get("createdAt"));
//				Assert.assertEquals(response.getBody().jsonPath().getString("data.transaction.updatedAt"), map.get("updatedAt"));
			}
			
			int discount = 0;
			int value = 0;
			int maxDeduction = 0;
			
			if (!voucherId.equals("0")) {
				param = new LinkedHashMap<String, Object>();
				query = "SELECT * FROM voucher WHERE id = ?";
				param.put("1", Long.parseLong(voucherId));
				data = sqlExec(query, param, "PROMOTION");
				
				if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
				for (Map<String, Object> map : data) {
					discount = (int) map.get("discount");
					value = (int) map.get("value");
					maxDeduction = (int) map.get("maxDeduction");
					
					if (discount * catalog.getPrice() > maxDeduction) discount = maxDeduction;
				}
			}
			
			param = new LinkedHashMap<String, Object>();
			query = "SELECT * FROM balance WHERE userId = ?";
			param.put("1", user.getId());
			data = sqlExec(query, param, "MEMBER");
			
			if (data.size() == 0) Assert.assertTrue(false, "no balance found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals((int) (user.getBalance() - (catalog.getPrice() - discount + value)), (int) map.get("balance"));
			}
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
