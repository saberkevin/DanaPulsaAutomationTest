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
import model.Transaction;
import model.User;

public class TC_Remote_Service_GetTransactionById extends TestBase {
	private User user = new User();
	private Transaction transaction = new Transaction();
	private String transactionId;
	
	public TC_Remote_Service_GetTransactionById() {
		
	}
	
	public TC_Remote_Service_GetTransactionById(String transactionId) {
		this.transactionId = transactionId;
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
		
		/// initialize user
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setUsername("081252930398");
		user.setPin(123456);
		
		// insert user into database and get user id from it
		deleteUserIfExist(user.getEmail(), user.getUsername());
		createUser(user);
		user.setId(getUserIdByUsername(user.getUsername()));
		
		
		// insert transaction into database - TELKOMSEL 30k
		transaction.setCatalogId(16);
		deleteTransactionByUserIdIfExist(user.getId());
		createTransaction(user.getId(), user.getUsername(), transaction.getCatalogId());
		transaction.setId(getTransactionIdByUserId(user.getId()));

		// if data from excel "true", then get valid transaction id
		if (transactionId.equals("true")) {
			transactionId = Long.toString(transaction.getId());
		}
	}
	
	@Test
	public void testTransactionById() {
		// call API get transaction by id
		getTransactionByIdRemoteService(transactionId);
		
		int statusCode = response.getStatusCode();

		if (statusCode != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testTransactionById"})
	public void checkData() throws ParseException {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			String responseBody = response.getBody().asString();
			
			if (!responseBody.equals("unknown transaction") && !responseBody.equals("invalid request format")) {
				Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("id")), transactionId);
				Assert.assertNotNull(response.getBody().jsonPath().get("methodId"));
				Assert.assertNotNull(response.getBody().jsonPath().get("phoneNumber"));
				Assert.assertNotNull(response.getBody().jsonPath().get("catalog.id"));
				Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.id"));
				Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.name"));
				Assert.assertNotNull(response.getBody().jsonPath().get("catalog.provider.image"));
				Assert.assertNotNull(response.getBody().jsonPath().get("catalog.value"));
				Assert.assertNotNull(response.getBody().jsonPath().get("catalog.price"));
	//			Assert.assertNotNull(response.getBody().jsonPath().get("voucher"));
				Assert.assertNotNull(response.getBody().jsonPath().get("status"));
				Assert.assertNotNull(response.getBody().jsonPath().get("createdAt"));
//				Assert.assertNotNull(response.getBody().jsonPath().get("updatedAt"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
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
						Assert.assertTrue(false, "no transaction data");
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
	}
	
	@AfterClass
	public void afterClass() {
		// delete transaction by id
		deleteTransactionById(transaction.getId());
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
