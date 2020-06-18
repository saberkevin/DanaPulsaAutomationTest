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
import model.Catalog;
import model.Transaction;
import model.User;

public class TC_Integration_CancelOrder extends TestBase {
	private User user = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private String testCase;
	private String result;
	
	public TC_Integration_CancelOrder() {
		
	}
	
	public TC_Integration_CancelOrder(String testCase, String result) {
		this.testCase = testCase;
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
		
		// get catalog TELKOMSEL 15K
		getCatalog(user.getSessionId(), user.getUsername().substring(0,6));
		checkStatusCode("200");
		List<Map<String, String>> vouchers = response.getBody().jsonPath().getList("data.catalog");
		catalog.setId(Long.parseLong(vouchers.get(3).get("id")));
		
		// create order
		createOrder(user.getSessionId(), user.getUsername(), Long.toString(catalog.getId()));
		checkStatusCode("201");
		transaction.setId(response.getBody().jsonPath().getLong("data.id"));
	}
	
	@Test
	public void testCancelOrder() {
		cancelOrder(user.getSessionId(), Long.toString(transaction.getId()));
		checkStatusCode("200");
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "deleted");
	}
	
	@Test(dependsOnMethods = {"testCancelOrder"})
	public void checkData() {
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.id"), transaction.getId());
		Assert.assertEquals(response.getBody().jsonPath().get("data.method"), "WALLET");
		Assert.assertEquals(response.getBody().jsonPath().get("data.phoneNumber"), user.getUsername());
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.id"), catalog.getId());
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.provider.id"), 2);
		Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.name"), "Telkomsel");
		Assert.assertEquals(response.getBody().jsonPath().get("data.catalog.provider.image"), 
				"https://res.cloudinary.com/alvark/image/upload/v1592209103/danapulsa/Telkomsel_Logo_eviigt_nbbrjv.png");
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.value"), 15000);
		Assert.assertEquals(response.getBody().jsonPath().getLong("data.catalog.price"), 15000);
		Assert.assertEquals(response.getBody().jsonPath().get("data.status"), "CANCELED");
		Assert.assertNotNull(response.getBody().jsonPath().get("data.createdAt"));
		Assert.assertNotNull(response.getBody().jsonPath().get("data.updatedAt"));
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
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
			ps.setLong(1, transaction.getId());
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
	
	@AfterClass
	public void afterClass() {
		// delete user
		deleteTransactionByUserId(user.getId());
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		

		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
