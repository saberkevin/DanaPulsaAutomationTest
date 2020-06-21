package integrationtest.payment;

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
import integrationtest.order.ConfigIntegrationTestOrder;
import model.Catalog;
import model.Provider;
import model.Transaction;
import model.User;

public class TC_Integration_Payment extends TestBase {
	private User user = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String voucherId;
	private String result;
	
	public TC_Integration_Payment(String testCase, String voucherId, String result) {
		this.testCase = testCase;
		this.voucherId = voucherId;
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
		
		// get catalog TELKOMSEL 1000K
		getCatalog(user.getSessionId(), user.getUsername().substring(0,5));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		List<Map<String, Object>> vouchers = response.getBody().jsonPath().getList("data.catalog");
		catalog.setId(Long.valueOf((Integer) vouchers.get(24).get("id")));
		
		// create order
		createOrder(user.getSessionId(), user.getUsername(), Long.toString(catalog.getId()));
		checkStatusCode("201");
		transaction.setId(response.getBody().jsonPath().getLong("data.id"));
		
		// insert voucher into database
		if (!voucherId.equals("0")) {
			createUserVoucher(user.getId(), Long.parseLong(voucherId), 2);
		}
	}
	
	@Test
	public void testPayOrder() {
		payOrder(user.getSessionId(), Long.toString(transaction.getId()), "1", voucherId);
		checkStatusCode("200");
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
	}
	
	@Test(dependsOnMethods = {"testPayOrder"})
	public void checkData() throws ParseException {
		if (response.getStatusCode() == 201) {
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
		
		query = "SELECT A.*, B.value, B.price, C.id AS providerId, C.name AS providerName, C.image AS providerImage, "
				+ "D.name AS transactionStatus, E.name AS paymentMethodName "
				+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
				+ "LEFT JOIN provider C on B.providerId = C.id "
				+ "LEFT JOIN transaction_status D on A.statusId = D.id "
				+ "LEFT JOIN payment_method E on A.methodId = E.id "
				+ "WHERE A.id = ? AND A.userId = ?";
		param.put("1", transaction.getId());
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
	
	@AfterClass
	public void afterClass() {
		deleteUserVoucherByUserId(user.getId());
		deleteTransactionByUserId(user.getId());
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
