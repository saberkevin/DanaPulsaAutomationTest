package integrationtest.order;

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

public class TC_Integration_GetAllCatalog extends TestBase {
	private User user = new User();
	private String testCase;
	private String phonePrefix;
	private String result;
	
	public TC_Integration_GetAllCatalog() {
		
	}
	
	public TC_Integration_GetAllCatalog(String testCase, String phonePrefix, String result) {
		this.testCase = testCase;
		this.phonePrefix = phonePrefix;
		this.result = result;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		// initialize user
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setUsername("081252930398");
		user.setPin(123456);
		
		// delete if exist
		deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
		deleteUserIfExist(user.getEmail(), user.getUsername());
		
		// register new user
		register(user.getName(), user.getEmail(), user.getUsername(), Integer.toString(user.getPin()));
		checkStatusCode("200");
		
		// login to system
		login("62" + user.getUsername().substring(1));
		checkStatusCode("200");
		user.setId(response.getBody().jsonPath().getLong("data.id"));
		
		// verify pin login
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
	}
	
	@Test
	public void testGetAllCatalog() {
		getCatalog(user.getSessionId(), phonePrefix);
		checkStatusCode("200");

		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
	}
	
	@Test(dependsOnMethods = {"testGetAllCatalog"})
	public void checkData() {
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
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
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
	
	@AfterClass
	public void afterClass() {
		// delete user
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());

		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
