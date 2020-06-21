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

public class TC_Remote_Service_GetTransactionByIdByUserId extends TestBase {
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
	private String dataAMQP;
	private JsonPath responseData;

	public TC_Remote_Service_GetTransactionByIdByUserId(String testCase, String userId, String transactionId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.transactionId = transactionId;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (userId.equals("true") || transactionId.equals("true")) {
			// initialize user
			user.setName("Zanuar");
			user.setEmail("triromadon@gmail.com");
			user.setUsername("081252930398");
			user.setPin(123456);
			
			// insert user into database
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());			
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));
			createBalance(user.getId(), 10000000);

			if (userId.equals("true"))
				userId = Long.toString(user.getId());
			
			// set flag
			isCreateUser = true;
		}
		
		if (transactionId.equals("true")) {	
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
			createTransaction(anotherUser.getId(), anotherUser.getUsername(), catalog.getId());
			transaction.setId(getTransactionIdByUserId(anotherUser.getId()));
			transaction.setPhoneNumber(anotherUser.getUsername());
			transaction.setCatalogId(catalog.getId());
			transaction.setMethodId(1);
			transaction.setStatus("COMPLETED");
			transaction.setPaymentMethodName("WALLET");			
			transactionId = Long.toString(transaction.getId());
		}
	}
	
	@Test
	public void testTransactionByIdByUserId() {		
		String message = "{\"userId\":" + userId + ",\"transactionId\":" + transactionId + "}";
		dataAMQP = callRP(orderAMQP, ConfigRemoteServiceOrder.QUEUE_TRANSACTION_BY_ID_BY_USER_ID, message);
		responseData = new JsonPath(dataAMQP);
		logger.info("message = " + message);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testTransactionByIdByUserId"})
	public void checkData() throws ParseException {
		Assert.assertTrue(dataAMQP.contains(result), dataAMQP);
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "unknown transaction";
		final String errorMessage3 = "invalid request format";
		
		if (dataAMQP.contains(errorMessage1)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage2)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage3)) {
			// do some code
		} else {
			Assert.assertEquals(responseData.getLong("id"), transaction.getId());
			Assert.assertEquals(responseData.get("method"), transaction.getPaymentMethodName());
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
			Assert.assertNull(responseData.get("updatedAt"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown user";
		final String errorMessage2 = "unknown transaction";
		final String errorMessage3 = "invalid request format";
		
		switch (dataAMQP) {
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
			
			if (data.size() == 0) Assert.assertTrue(false, "no transaction found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(responseData.getLong("id"), map.get("id"));
				Assert.assertEquals(responseData.getString("method"), map.get("paymentMethodName"));
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
		
		if (testCase.equals("Another user's transaction")) {
			deleteTransactionByUserId(anotherUser.getId());
			deleteBalanceByUserId(anotherUser.getId());
			deleteUserByEmailAndUsername(anotherUser.getEmail(), anotherUser.getUsername());			
		}

		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
