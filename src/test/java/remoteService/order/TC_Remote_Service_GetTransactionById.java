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

public class TC_Remote_Service_GetTransactionById extends TestBase {
	private User user = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String transactionId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Remote_Service_GetTransactionById(String testCase, String transactionId, String result) {
		this.testCase = testCase;
		this.transactionId = transactionId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void getTransactionByIdRemoteService(String transactionId) {
		logger.info("Call Get Transaction By Id API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("transaction id:" + transactionId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_TRANSACTION_BY_ID);
		requestParams.put("message", transactionId);
		
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
		
		if (transactionId.equals("true")) {
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
			createTransaction(user.getId(), user.getUsername(), catalog.getId());
			transaction.setId(getTransactionIdByUserId(user.getId()));
			transaction.setPhoneNumber(user.getUsername());
			transaction.setCatalogId(catalog.getId());
			transaction.setMethodId(1);
			transaction.setStatus("COMPLETED");			
			transactionId = Long.toString(transaction.getId());
			
			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testTransactionById() {
		getTransactionByIdRemoteService(transactionId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testTransactionById"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		final String errorMessage1 = "unknown transaction";
		final String errorMessage2 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else {
			Assert.assertEquals(response.getBody().jsonPath().getLong("id"), transaction.getId());
			Assert.assertEquals(response.getBody().jsonPath().getLong("methodId"), transaction.getMethodId());
			Assert.assertEquals(response.getBody().jsonPath().get("phoneNumber"), transaction.getPhoneNumber());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.id"), transaction.getCatalogId());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.provider.id"), provider.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("catalog.provider.name"), provider.getName());
			Assert.assertEquals(response.getBody().jsonPath().get("catalog.provider.image"), provider.getImage());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.value"), catalog.getValue());
			Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.price"), catalog.getPrice());
			Assert.assertNull(response.getBody().jsonPath().get("voucher"));
			Assert.assertEquals(response.getBody().jsonPath().get("status"), transaction.getStatus());
			Assert.assertNotNull(response.getBody().jsonPath().get("createdAt"));
//				Assert.assertNotNull(response.getBody().jsonPath().get("updatedAt"));			
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown transaction";
		final String errorMessage2 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM transaction WHERE id = ?";
			param.put("1", Long.parseLong(transactionId));
			data = sqlExec(query, param, "order");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			// do some code
			break;
		default:
			query = "SELECT A.*, B.value, B.price, C.id AS providerId, C.name AS providerName, C.image AS providerImage, D.name AS transactionStatus "
					+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
					+ "LEFT JOIN provider C on B.providerId = C.id "
					+ "LEFT JOIN transaction_status D on A.statusId = D.id "
					+ "WHERE A.id = ?";
			param.put("1", Long.parseLong(transactionId));
			data = sqlExec(query, param, "order");
			
			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().jsonPath().getLong("id"), map.get("id"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("methodId"), map.get("methodId"));
				Assert.assertEquals(response.getBody().jsonPath().getString("phoneNumber"), map.get("phoneNumber"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.provider.id"), map.get("providerId"));
				Assert.assertEquals(response.getBody().jsonPath().getString("catalog.provider.name"), map.get("providerName"));
				Assert.assertEquals(response.getBody().jsonPath().getString("catalog.provider.image"), map.get("providerImage"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.value"), map.get("value"));
				Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.price"), map.get("price"));
				Assert.assertEquals(response.getBody().jsonPath().getString("status"), map.get("transactionStatus"));
//					Assert.assertEquals(response.getBody().jsonPath().getString("createdAt"), map.get("createdAt"));
//					Assert.assertEquals(response.getBody().jsonPath().getString("updatedAt"), map.get("updatedAt"));
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
