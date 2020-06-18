package integrationtest.order;

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

public class TC_Integration_CreateOrder extends TestBase {
	private User user = new User();
	private String testCase;
	private String phoneNumber;
	private String catalogId;
	private String result;
	
	public TC_Integration_CreateOrder() {
		
	}
	
	public TC_Integration_CreateOrder(String testCase, String phoneNumber, String catalogId, String result) {
		this.testCase = testCase;
		this.phoneNumber = phoneNumber;
		this.catalogId = catalogId;
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
		checkStatusCode("201");
		
		// login to system
		login("62" + user.getUsername().substring(1));
		checkStatusCode("200");
		user.setId(response.getBody().jsonPath().getLong("data.id"));
		
		// verify pin login
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		// get catalog
		getCatalog(user.getSessionId(), user.getUsername().substring(0,5));
		checkStatusCode("200");
	}
	
	@Test
	public void testCreateOrder() {	
		createOrder(user.getSessionId(), phoneNumber, catalogId);
		checkStatusCode("201");

		Assert.assertTrue(response.getBody().asString().contains(result));
		
		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "201");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "created");
	}
	
	@Test(dependsOnMethods = {"testCreateOrder"})
	public void checkData() {
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.id")));
		Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), phoneNumber);
		Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("data.catalog.id")), catalogId);
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.provider.id")));
		Assert.assertNotNull(response.getBody().jsonPath().get("data.catalog.provider.name"));
		Assert.assertNotNull(response.getBody().jsonPath().get("data.catalog.provider.image"));
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.value")));
		Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.price")));
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = getConnectionOrder();
			String queryString = "SELECT "
					+ "COUNT(*) AS count, "
					+ "A.*, "
					+ "B.value, "
					+ "B.price, "
					+ "C.id AS providerId, "
					+ "C.name AS providerName, "
					+ "C.image AS providerImage "
					+ "FROM transaction A LEFT JOIN pulsa_catalog on A.catalogId = B.id "
					+ "LEFT JOIN provider C on B.providerId = C.id "
					+ "WHERE A.userId = ? AND A.phoneNumber = ? AND A.catalogId = ?";
			
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
				Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), rs.getString("phoneNumber"));
				Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("data.catalog.id")), rs.getString("catalogId"));
				Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.provider.id")), rs.getString("providerId"));
				Assert.assertNotNull(response.getBody().jsonPath().get("data.catalog.provider.name"), rs.getString("providerName"));
				Assert.assertNotNull(response.getBody().jsonPath().get("data.catalog.provider.image"), rs.getString("providerImage"));
				Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.value")), rs.getString("value"));
				Assert.assertNotNull(Integer.toString(response.getBody().jsonPath().get("data.catalog.price")), rs.getString("price"));
			} while(rs.next());
			
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public void afterClass() {
		// delete user
		deleteTransactionByUserId(user.getId());
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());

		// delete transaction
		deleteTransactionByPhoneNumber(phoneNumber);			
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
