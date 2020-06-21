package remoteService.order;

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
import io.restassured.path.json.JsonPath;
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
	private String dataAMQP;
	private JsonPath responseData;
	
	public TC_Remote_Service_GetTransactionById(String testCase, String transactionId, String result) {
		this.testCase = testCase;
		this.transactionId = transactionId;
		this.result = result;
		isCreateUser = false;
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
		dataAMQP = callRP(orderAMQP, ConfigRemoteServiceOrder.QUEUE_TRANSACTION_BY_ID, transactionId);
		responseData = new JsonPath(dataAMQP);
		logger.info("message = " + transactionId);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testTransactionById"})
	public void checkData() throws ParseException {
		Assert.assertTrue(dataAMQP.contains(result), dataAMQP);
		
		final String errorMessage1 = "unknown transaction";
		final String errorMessage2 = "invalid request format";
		
		if (dataAMQP.contains(errorMessage1)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage2)) {
			// do some code
		} else {
			Assert.assertEquals(responseData.getLong("id"), transaction.getId());
			Assert.assertEquals(responseData.getLong("methodId"), transaction.getMethodId());
			Assert.assertEquals(responseData.get("phoneNumber"), transaction.getPhoneNumber());
			Assert.assertEquals(responseData.getLong("catalog.id"), transaction.getCatalogId());
			Assert.assertEquals(responseData.getLong("catalog.provider.id"), provider.getId());
			Assert.assertEquals(responseData.get("catalog.provider.name"), provider.getName());
			Assert.assertEquals(responseData.get("catalog.provider.image"), provider.getImage());
			Assert.assertEquals(responseData.getLong("catalog.value"), catalog.getValue());
			Assert.assertEquals(responseData.getLong("catalog.price"), catalog.getPrice());
			Assert.assertNull(responseData.get("voucher"));
			Assert.assertEquals(responseData.get("status"), transaction.getStatus());
			Assert.assertNotNull(responseData.get("createdAt"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown transaction";
		final String errorMessage2 = "invalid request format";
		
		switch (dataAMQP) {
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
				Assert.assertEquals(responseData.getLong("id"), map.get("id"));
				Assert.assertEquals(responseData.getLong("methodId"), map.get("methodId"));
				Assert.assertEquals(responseData.getString("phoneNumber"), map.get("phoneNumber"));
				Assert.assertEquals(responseData.getLong("catalog.provider.id"), map.get("providerId"));
				Assert.assertEquals(responseData.getString("catalog.provider.name"), map.get("providerName"));
				Assert.assertEquals(responseData.getString("catalog.provider.image"), map.get("providerImage"));
				Assert.assertEquals(responseData.getLong("catalog.value"), map.get("value"));
				Assert.assertEquals(responseData.getLong("catalog.price"), map.get("price"));
				Assert.assertEquals(responseData.getString("status"), map.get("transactionStatus"));
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
