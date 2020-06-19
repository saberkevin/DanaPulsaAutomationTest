package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.Catalog;
import model.Provider;
import model.Transaction;
import model.User;

public class TC_Cancel_Order extends TestBase {
	private User user = new User();
	private User anotherUser = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String sessionId;
	private String transactionId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Cancel_Order() {
		
	}
	
	public TC_Cancel_Order(String testCase, String sessionId, String transactionId, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.transactionId = transactionId;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (sessionId.equals("true") || transactionId.equals("true")) {
			isCreateUser = true;
			
			// initialize user
			user.setName("Zanuar");
			user.setEmail("triromadon@gmail.com");
			user.setUsername("081252930398");
			user.setPin(123456);
			
			// delete if exist
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());
			
			// insert user into database
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));
			
			if (sessionId.equals("true")) {				
				verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
				checkStatusCode("200");
				user.setSessionId(response.getCookie("JSESSIONID"));
				sessionId = user.getSessionId();		
			}
			
			// insert balance into database
			createBalance(user.getId(), 10000000);
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
			if (testCase.equals("Transaction already completed")) {
				createTransaction(user.getId(), user.getUsername(), catalog.getId(), 1);
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
			
			// delete if exist
			deleteBalanceByEmailByUsername(anotherUser.getEmail(), anotherUser.getUsername());
			deleteUserIfExist(anotherUser.getEmail(), anotherUser.getUsername());
			
			// insert user into database
			createUser(anotherUser);
			anotherUser.setId(getUserIdByUsername(anotherUser.getUsername()));
			
			// insert balance into database
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
			
			// initialize transaction
			transaction.setId(getTransactionIdByUserId(anotherUser.getId()));
			transaction.setPhoneNumber(anotherUser.getUsername());
			transaction.setCatalogId(catalog.getId());
			transaction.setMethodId(1);
			transaction.setPaymentMethodName("WALLET");
			
			transactionId = Long.toString(transaction.getId());
		}
	}
	
	@Test
	public void testCancelOrder() {
		cancelOrder(sessionId, transactionId);
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();
		
		if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "can't cancel completed transaction");
		} else if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertTrue(response.getBody().asString().contains("Not Found") 
					|| response.getBody().asString().contains("unknown transaction"));
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "deleted");
		}
	}
	
	@Test(dependsOnMethods = {"testCancelOrder"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.id"), transaction.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("data.method"), transaction.getPaymentMethodName());
			Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), transaction.getPhoneNumber());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.id"), transaction.getCatalogId());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.provider.id"), provider.getId());
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.name"), provider.getName());
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.image"), provider.getImage());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.value"), catalog.getValue());
			Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.price"), catalog.getPrice());
			Assert.assertEquals(response.getBody().jsonPath().get("data.status"), "CANCELED");
			Assert.assertNotNull(response.getBody().jsonPath().get("data.createdAt"));
			Assert.assertNotNull(response.getBody().jsonPath().get("data.updatedAt"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 400) {
			if (response.getBody().asString().equals("can't cancel completed transaction")) {
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT * FROM transaction WHERE id = ? AND userId = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, Long.parseLong(transactionId));
					ps.setLong(2, user.getId());
					
					ResultSet rs = ps.executeQuery();
					
					if (!rs.next()) {
						Assert.assertTrue(false, "no transaction found in database");
					}
					do {
						Assert.assertEquals("1", rs.getString("statusId"));
					} while(rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else if (statusCode == 404) {
			if (response.getBody().asString().equals("unknown transaction")) {
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT * FROM transaction WHERE id = ? AND userId = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, Long.parseLong(transactionId));
					ps.setLong(2, user.getId());
					
					ResultSet rs = ps.executeQuery();
					Assert.assertTrue(!rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
		} else if (statusCode == 200) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT "
						+ "A.*, "
						+ "B.value, "
						+ "B.price, "
						+ "C.id AS providerId, "
						+ "C.name AS providerName, "
						+ "C.image AS providerImage, "
						+ "D.name AS transactionStatus, "
						+ "E.name AS paymentMethodName "
						+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
						+ "LEFT JOIN provider C on B.providerId = C.id "
						+ "LEFT JOIN transaction_status D on A.statusId = D.id "
						+ "LEFT JOIN payment_method E on A.methodId = E.id "
						+ "WHERE A.id = ? AND A.userId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(transactionId));
				ps.setLong(2, user.getId());
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no transaction found in database");
				}
				do {
					Assert.assertEquals(response.getBody().jsonPath().getLong("data.id"), rs.getLong("id"));
					Assert.assertEquals(response.getBody().jsonPath().getString("data.method"), rs.getString("paymentMethodName"));
					Assert.assertEquals(response.getBody().jsonPath().getString("data.phoneNumber"), rs.getString("phoneNumber"));
					Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.provider.id"), rs.getLong("providerId"));
					Assert.assertEquals(response.getBody().jsonPath().getString("data.catalog.provider.name"), rs.getString("providerName"));
					Assert.assertEquals(response.getBody().jsonPath().getString("data.catalog.provider.image"), rs.getString("providerImage"));
					Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.value"), rs.getLong("value"));
					Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.price"), rs.getLong("price"));
					Assert.assertEquals(response.getBody().jsonPath().getString("data.status"), rs.getString("transactionStatus"));
//					Assert.assertEquals(response.getBody().jsonPath().getString("createdAt"), rs.getString("createdAt"));
//					Assert.assertEquals(response.getBody().jsonPath().getString("updatedAt"), rs.getString("updatedAt"));
				} while(rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		// delete user
		if (isCreateUser == true) {
			deleteTransactionByUserId(user.getId());
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		
		// delete another user
		if (testCase.equals("Another user's transaction")) {
			deleteTransactionByUserId(anotherUser.getId());
			deleteBalanceByUserId(anotherUser.getId());
			deleteUserByEmailAndUsername(anotherUser.getEmail(), anotherUser.getUsername());			
		}

		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
