package integrationtest.order;

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
import model.Transaction;
import model.User;

public class TC_Integration_CancelOrder extends TestBase {
	private User user = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private String testCase;
	private String result;
	
	public TC_Integration_CancelOrder(String testCase, String result) {
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
		getCatalog(user.getSessionId(), user.getUsername().substring(0,6));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		List<Map<String, Object>> vouchers = response.getBody().jsonPath().getList("data.catalog");
		catalog.setId(Long.valueOf((Integer) vouchers.get(3).get("id")));
		
		// create order
		createOrder(user.getSessionId(), user.getUsername(), Long.toString(catalog.getId()));
		checkStatusCode("201");
		transaction.setId(response.getBody().jsonPath().getLong("data.id"));
	}
	
	@Test
	public void testCancelOrder() {
		cancelOrder(user.getSessionId(), Long.toString(transaction.getId()));
		checkStatusCode("200");
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "deleted");
	}
	
	@Test(dependsOnMethods = {"testCancelOrder"})
	public void checkData() {
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.id"), transaction.getId());
		Assert.assertEquals(response.getBody().jsonPath().get("data.method"), "WALLET");
		Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), user.getUsername());
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.id"), catalog.getId());
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.provider.id"), 2L);
		Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.name"), "Telkomsel");
		Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.image"), 
				"https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.value"), 30000L);
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.price"), 30000L);
		Assert.assertEquals(response.getBody().jsonPath().get("data.status"), "CANCELED");
		Assert.assertNotNull(response.getBody().jsonPath().get("data.createdAt"));
		Assert.assertNotNull(response.getBody().jsonPath().get("data.updatedAt"));
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
//			Assert.assertEquals(response.getBody().jsonPath().getString("data.createdAt"), map.get("createdAt"));
//			Assert.assertEquals(response.getBody().jsonPath().getString("data.updatedAt"), map.get("updatedAt"));
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
