package remoteService.promotion;

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

public class TC_Remote_Service_GetVoucherRecommendation extends TestBase {
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
	
	public TC_Remote_Service_GetVoucherRecommendation() {
		
	}
	
	public TC_Remote_Service_GetVoucherRecommendation(String testCase, String userId, String transactionId, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.transactionId = transactionId;
		this.result = result;
		isCreateUser = false;
	}
	
	@SuppressWarnings("unchecked")
	public void getVoucherRecommendationRemoteService(String userId, String transactionId) {
		logger.info("Call Get Voucher Recommendation API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("user id:" + userId);
		logger.info("transactionId:" + transactionId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", ConfigRemoteServicePromotion.QUEUE_GET_VOUCHER_RECOMMENDATION);
		requestParams.put("request", "{\"userId\":" + userId + ",\"transactionId\":" + transactionId + "}");
		
		RestAssured.baseURI = ConfigRemoteServicePromotion.BASE_URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, ConfigRemoteServicePromotion.ENDPOINT_PATH);
		logger.info(response.getBody().asString());
	}

	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (userId.equals("true") || transactionId.equals("true")) {
			// initialize user
			user.setName(ConfigRemoteServicePromotion.USER_NAME);
			user.setEmail(ConfigRemoteServicePromotion.USER_EMAIL);
			user.setUsername(ConfigRemoteServicePromotion.USER_USERNAME);
			user.setPin(ConfigRemoteServicePromotion.USER_PIN);
			
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
			if (testCase.equals("Valid user id (meets minimum purchase)")) {
				catalog.setId(16);
				catalog.setProviderId(2);
				catalog.setValue(30000);
				catalog.setPrice(30000);
			} else {
				catalog.setId(13);
				catalog.setProviderId(2);
				catalog.setValue(15000);
				catalog.setPrice(15000);
			}

			// initialize provider - TELKOMSEL
			provider.setId(2);
			provider.setName("Telkomsel");
			provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");

			// insert transaction into database
			createTransaction(user.getId(), user.getUsername(), catalog.getId(), 4);
			transaction.setId(getTransactionIdByUserId(user.getId()));
			transactionId = Long.toString(transaction.getId());
			
			// insert voucher into database
			if (!testCase.equals("Valid user id (have no vouchers)")) {
				createUserVoucher(user.getId(), 4, 2);
			}
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
			catalog.setId(16);
			catalog.setProviderId(2);
			catalog.setValue(30000);
			catalog.setPrice(30000);
			
			// initialize provider - TELKOMSEL
			provider.setId(2);
			provider.setName("Telkomsel");
			provider.setImage("https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");

			// insert transaction into database
			createTransaction(anotherUser.getId(), anotherUser.getUsername(), catalog.getId(), 4);
			transaction.setId(getTransactionIdByUserId(anotherUser.getId()));			
			transactionId = Long.toString(transaction.getId());
		}
	}
	
	@Test
	public void testVoucherRecommendation() {
		getVoucherRecommendationRemoteService(userId, transactionId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testVoucherRecommendation"})
	public void checkData() {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		final String errorMessage1 = "user not found";
		final String errorMessage2 = "transaction not found or expired";
		final String errorMessage3 = "[]";
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
			List<Map<String, String>> vouchers = response.jsonPath().get();

			Assert.assertTrue(vouchers.size() <= 10, "maximum vouchers is 10");
			
			for (int i = 0; i < vouchers.size(); i++) {
				Assert.assertNotNull(vouchers.get(i).get("id"));
				Assert.assertNotNull(vouchers.get(i).get("name"));
				Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
				Assert.assertNotNull(vouchers.get(i).get("discount"));
				Assert.assertNotNull(vouchers.get(i).get("maxDeduction"));
				Assert.assertNotNull(vouchers.get(i).get("value"));
				Assert.assertNotNull(vouchers.get(i).get("filePath"));
				Assert.assertNotNull(vouchers.get(i).get("expiryDate"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";

		final String errorMessage1 = "user not found";
		final String errorMessage2 = "transaction not found or expired";
		final String errorMessage3 = "[]";
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
			query = "SELECT A.id, A.name, D.name AS voucherTypeName, A.value, A.discount, A.maxDeduction, A.filePath, A.expiryDate "
					+ "FROM voucher AS A OIN user_voucher AS B ON B.voucherId = A.id "
					+ "JOIN user_voucher_status AS C ON B.voucherStatusId = C.id "
					+ "JOIN voucher_type AS D ON D.id = A.typeId "
					+ "JOIN voucher_provider AS E ON E.voucherId = A.id "
					+ "JOIN issue_voucher_rule AS F ON F.voucherId = A.id "
					+ "JOIN voucher_payment_method AS G ON G.voucherId = A.id "
					+ "WHERE B.userId = ? AND B.voucherStatusId != 1 AND G.paymentMethodId = 1 AND E.providerId = ? AND F.minPurchase <= ? "
					+ "ORDER BY A.maxDeduction DESC";
			param.put("1", Long.parseLong(userId));
			param.put("2", provider.getId());
			param.put("3", catalog.getPrice());
			data = sqlExec(query, param, "promotion");
			Assert.assertTrue(data.size() == 0);			
			break;			
		case errorMessage3:
			query = "SELECT A.id, A.name, D.name AS voucherTypeName, A.value, A.discount, A.maxDeduction, A.filePath, A.expiryDate "
					+ "FROM voucher AS A OIN user_voucher AS B ON B.voucherId = A.id "
					+ "JOIN user_voucher_status AS C ON B.voucherStatusId = C.id "
					+ "JOIN voucher_type AS D ON D.id = A.typeId "
					+ "JOIN voucher_provider AS E ON E.voucherId = A.id "
					+ "JOIN issue_voucher_rule AS F ON F.voucherId = A.id "
					+ "JOIN voucher_payment_method AS G ON G.voucherId = A.id "
					+ "WHERE B.userId = ? AND B.voucherStatusId != 1 AND G.paymentMethodId = 1 AND E.providerId = ? AND F.minPurchase <= ? "
					+ "ORDER BY A.maxDeduction DESC";
			param.put("1", Long.parseLong(userId));
			param.put("2", provider.getId());
			param.put("3", catalog.getPrice());
			data = sqlExec(query, param, "promotion");
			Assert.assertTrue(data.size() == 0);
			break;	
		case errorMessage4:
			// do some code
			break;
		default:
			query = "SELECT A.id, A.name, D.name AS voucherTypeName, A.value, A.discount, A.maxDeduction, A.filePath, A.expiryDate "
					+ "FROM voucher AS A JOIN user_voucher AS B ON B.voucherId = A.id "
					+ "JOIN user_voucher_status AS C ON B.voucherStatusId = C.id "
					+ "JOIN voucher_type AS D ON D.id = A.typeId "
					+ "JOIN voucher_provider AS E ON E.voucherId = A.id "
					+ "JOIN issue_voucher_rule AS F ON F.voucherId = A.id "
					+ "JOIN voucher_payment_method AS G ON G.voucherId = A.id "
					+ "WHERE B.userId = ? AND B.voucherStatusId != 1 AND G.paymentMethodId = 1 AND E.providerId = ? AND F.minPurchase <= ? "
					+ "ORDER BY A.maxDeduction DESC";
			param.put("1", Long.parseLong(userId));
			param.put("2", provider.getId());
			param.put("3", catalog.getPrice());
			data = sqlExec(query, param, "promotion");
			
			List<Map<String, String>> vouchers = response.jsonPath().get();
			int index = 0;

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(String.valueOf(vouchers.get(index).get("id")), (String) map.get("id"));
				Assert.assertEquals(vouchers.get(index).get("name"), map.get("name"));
				Assert.assertEquals(String.valueOf(vouchers.get(index).get("discount")),(String) map.get("discount"));
				Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), map.get("voucherTypeName"));
				Assert.assertEquals(String.valueOf(vouchers.get(index).get("maxDeduction")), (String) map.get("maxDeduction"));
				Assert.assertEquals(String.valueOf(vouchers.get(index).get("value")), (String) map.get("value"));
				Assert.assertEquals(vouchers.get(index).get("filePath"), map.get("filePath"));
//				Assert.assertEquals(vouchers.get(index).get("expiryDate"), map.get("expiryDate"));
				index++;
			}
			break;
		}
	}

	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteTransactionByUserId(user.getId());
			deleteUserVoucherByUserId(user.getId());
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
