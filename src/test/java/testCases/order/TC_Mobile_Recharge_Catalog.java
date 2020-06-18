package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.User;

public class TC_Mobile_Recharge_Catalog extends TestBase {
	private User user = new User();
	private String testCase;
	private String sessionId;
	private String phonePrefix;
	private String result;
	private boolean isCreateUser;
	
	public TC_Mobile_Recharge_Catalog() {
		
	}
	
	public TC_Mobile_Recharge_Catalog(String testCase, String sessionId, String phonePrefix, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.phonePrefix = phonePrefix;
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
	public void testMobileRechargeCatalog() {
		getCatalog(sessionId, phonePrefix);
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();
		
		if (statusCode == 400) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "invalid phone number");
		} else if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertTrue(response.getBody().asString().contains("Not Found") 
					|| response.getBody().asString().contains("unknown phone number"));
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
		}
	}
	
	@Test(dependsOnMethods = {"testMobileRechargeCatalog"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			Map<String, String> provider = response.getBody().jsonPath().getMap("data.provider");
					
			Assert.assertNotNull(provider.get("id"));
			Assert.assertNotNull(provider.get("name"));
			Assert.assertNotNull(provider.get("image"));
			
			List<Map<String, String>> catalog = response.getBody().jsonPath().getList("data.catalog");
			
			for (int i = 0; i < catalog.size(); i++) {
				Assert.assertNotNull(catalog.get(i).get("id"));
				Assert.assertNotNull(catalog.get(i).get("value"));
				Assert.assertNotNull(catalog.get(i).get("price"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 404) {
			if (response.getBody().asString().contains("unknown phone number")) {
				try {
					Connection conn = getConnectionOrder();
					String queryString = "SELECT * FROM provider_prefix WHERE prefix = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setString(1, phonePrefix);
					
					ResultSet rs = ps.executeQuery();
					Assert.assertTrue(!rs.next());
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else if (statusCode == 400) {
			// do some code
			
		} else if (statusCode == 200) {
			List<Map<String, String>> catalog = response.getBody().jsonPath().getList("data.catalog");

			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT "
						+ "A.*, "
						+ "B.id AS providerId, "
						+ "B.name AS providerName, "
						+ "B.image AS providerImage "
						+ "FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id "
						+ "LEFT JOIN provider_prefix C on B.id = C.providerId "
						+ "WHERE C.prefix = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setString(1, phonePrefix.substring(1));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no catalog found in database");
				}
				do {
					Assert.assertEquals(response.getBody().jsonPath().getLong("data.provider.id"), rs.getLong("providerId"));
					Assert.assertEquals(response.getBody().jsonPath().getString("data.provider.name"), rs.getString("providerName"));
					Assert.assertEquals(response.getBody().jsonPath().getString("data.provider.image"), rs.getString("providerImage"));
					Assert.assertEquals(String.valueOf(catalog.get(rs.getRow()-1).get("id")), rs.getString("id"));
					Assert.assertEquals(String.valueOf(catalog.get(rs.getRow()-1).get("value")), rs.getString("value"));
					Assert.assertEquals(String.valueOf(catalog.get(rs.getRow()-1).get("price")), rs.getString("price"));
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
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}

		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
