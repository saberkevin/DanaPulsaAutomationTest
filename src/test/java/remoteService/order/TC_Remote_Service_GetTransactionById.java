package remoteService.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

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
	
	public TC_Remote_Service_GetTransactionById() {
		
	}
	
	public TC_Remote_Service_GetTransactionById(String testCase, String transactionId, String result) {
		this.testCase = testCase;
		this.transactionId = transactionId;
		this.result = result;
	}
	
	@SuppressWarnings("unchecked")
	public void getTransactionByIdRemoteService(String transactionId) {
		logger.info("Call Get Transaction By Id API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("transaction id:" + transactionId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", "getTransactionById");
		requestParams.put("message", transactionId);
		
		RestAssured.baseURI = URIOrder;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.POST, "/api/test/");
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (transactionId.equals("true")) {
			// initialize user
			user.setName("Zanuar");
			user.setEmail("triromadon@gmail.com");
			user.setUsername("081252930398");
			user.setPin(123456);
			
			// insert user into database
			deleteUserIfExist(user.getEmail(), user.getUsername());
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));
			
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
			deleteTransactionByUserIdIfExist(user.getId());
			createTransaction(user.getId(), user.getUsername(), catalog.getId());
			
			// initialize transaction
			transaction.setId(getTransactionIdByUserId(user.getId()));
			transaction.setPhoneNumber(user.getUsername());
			transaction.setCatalogId(catalog.getId());
			transaction.setMethodId(1);
			transaction.setStatus("COMPLETED");
			
			transactionId = Long.toString(transaction.getId());
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
		
		if (!responseBody.equals("unknown transaction") && !responseBody.equals("invalid request format")) {
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
		String responseBody = response.getBody().asString();

		if (responseBody.equals("unknown transaction")) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(transactionId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("invalid request format")) {
			// do some code
			
		} else {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT "
						+ "A.*, "
						+ "B.value, "
						+ "B.price, "
						+ "C.id AS providerId, "
						+ "C.name AS providerName, "
						+ "C.image AS providerImage, "
						+ "D.name AS transactionStatus "
						+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
						+ "LEFT JOIN provider C on B.providerId = C.id "
						+ "LEFT JOIN transaction_status D on A.statusId = D.id "
						+ "WHERE A.id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(transactionId));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no transaction found in database");
				}
				do {
					Assert.assertEquals(response.getBody().jsonPath().getLong("id"), rs.getLong("id"));
					Assert.assertEquals(response.getBody().jsonPath().getString("methodId"), rs.getString("methodId"));
					Assert.assertEquals(response.getBody().jsonPath().getString("phoneNumber"), rs.getString("phoneNumber"));
					Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.provider.id"), rs.getLong("providerId"));
					Assert.assertEquals(response.getBody().jsonPath().getString("catalog.provider.name"), rs.getString("providerName"));
					Assert.assertEquals(response.getBody().jsonPath().getString("catalog.provider.image"), rs.getString("providerImage"));
					Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.value"), rs.getLong("value"));
					Assert.assertEquals(response.getBody().jsonPath().getLong("catalog.price"), rs.getLong("price"));
					Assert.assertEquals(response.getBody().jsonPath().getString("status"), rs.getString("transactionStatus"));
//						Assert.assertEquals(response.getBody().jsonPath().getString("createdAt"), rs.getString("createdAt"));
//						Assert.assertEquals(response.getBody().jsonPath().getString("updatedAt"), rs.getString("updatedAt"));
				} while(rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
