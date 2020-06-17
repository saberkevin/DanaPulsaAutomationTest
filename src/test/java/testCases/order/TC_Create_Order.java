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
import model.User;

public class TC_Create_Order extends TestBase {
	private User user = new User();
	private String testCase;
	private String sessionId;
	private String phoneNumber;
	private String catalogId;
	private String result;
	private boolean isCreateUser;
	
	public TC_Create_Order() {
		
	}
	
	public TC_Create_Order(String testCase, String sessionId, String phoneNumber, String catalogId, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.phoneNumber = phoneNumber;
		this.catalogId = catalogId;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (sessionId.equals("true")) {
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
			
			verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
			checkStatusCode("200");
			user.setSessionId(response.getCookie("JSESSIONID"));
			sessionId = user.getSessionId();

			// insert balance into database
			createBalance(user.getId(), 10000000);
		}
	}
	
	@Test
	public void testCreateOrder() {	
		createOrder(sessionId, phoneNumber, catalogId);
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();
		
		if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertTrue(response.getBody().jsonPath().getString("message").equals("invalid phone number")
					|| response.getBody().jsonPath().getString("message").equals("selected catalog is not available for this phone’s provider")
					|| response.getBody().jsonPath().getString("message").equals("phone number must not be null")
					|| response.getBody().jsonPath().getString("message").equals("catalog ID must not be null"));
		} else if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "404");
			Assert.assertTrue(response.getBody().jsonPath().getString("message").equals("catalog not found")
					|| response.getBody().jsonPath().getString("message").equals("unknown phone number"));
		} else if (statusCode == 409) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "409");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "you’ve already requested this exact order within the last 30 seconds, "
					+ "please try again later if you actually intended to do that");
		} else if (statusCode == 201) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "201");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "created");
		}
	}
	
	@Test(dependsOnMethods = {"testCreateOrder"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 201) {
			Assert.assertNotNull(String.valueOf(response.getBody().jsonPath().get("data.id")));
			Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), phoneNumber);
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.id"), catalogId);
			
			Assert.assertEquals(String.valueOf(response.getBody().jsonPath().get("data.catalog.provider.id")), 2);
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.name"), "Telkomsel");
			Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.image"), 
					"https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
			
			Assert.assertEquals(String.valueOf(response.getBody().jsonPath().get("data.catalog.value")), "15000");
			Assert.assertEquals(String.valueOf(response.getBody().jsonPath().get("data.catalog.price")), "15000");
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 400) {
			if (response.getBody().asString().contains("selected catalog is not available for this phone’s provider")) {
				String providerName = "";
				
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT name FROM provider WHERE id IN (SELECT providerId FROM provider_prefix WHERE prefix = ?)";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setString(1, phoneNumber.substring(1,5));
					
					ResultSet rs = ps.executeQuery();
					
					if (!rs.next()) {
						Assert.assertTrue(false, "no provider found in database");
					}
					providerName = rs.getString("name");
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT B.name "
							+ "FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id "
							+ "WHERE A.id = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, Long.parseLong(catalogId));
					
					ResultSet rs = ps.executeQuery();
					if (!rs.next()) {
						Assert.assertTrue(false, "no provider found in database");
					}
					Assert.assertNotEquals(rs.getString("name"), providerName);
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (response.getBody().asString().contains("invalid phone number")) {
				// do some code
				
			}
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setString(2, phoneNumber);
				ps.setLong(3, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (statusCode == 404) {
			if (response.getBody().asString().contains("catalog not found")) {
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT * FROM pulsa_catalog WHERE id = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, Long.parseLong(catalogId));
					
					ResultSet rs = ps.executeQuery();
					Assert.assertTrue(!rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (response.getBody().asString().contains("unknown phone number")) {
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT * FROM provider_prefix WHERE prefix = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setString(1, phoneNumber.substring(1,5));
					
					ResultSet rs = ps.executeQuery();
					Assert.assertTrue(!rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setString(2, phoneNumber);
				ps.setLong(3, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (statusCode == 409) {
			// do some code
			
		} else if (statusCode == 201) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT COUNT(*) AS count FROM transaction WHERE userId = ? AND phoneNumber = ? AND catalogId = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setString(2, phoneNumber);
				ps.setLong(3, Long.parseLong(catalogId));
				
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
					Assert.assertTrue(false, "no transaction found in database");
				}
				do {
					Assert.assertEquals(rs.getInt("count"), 1);
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
		
		// delete transaction
		deleteTransactionByPhoneNumber(phoneNumber);			
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
