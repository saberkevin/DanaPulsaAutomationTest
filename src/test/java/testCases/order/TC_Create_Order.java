package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.Transaction;
import model.User;

public class TC_Create_Order extends TestBase {
	private User user;
	private String sessionId;
	private Transaction transaction;
	
	public TC_Create_Order(String sessionId, String phoneNumber, String catalogId) {
		this.sessionId = sessionId;
		transaction.setPaymentMethod(phoneNumber);
		transaction.getCatalog().setId(catalogId);
	}
	
	@BeforeClass
	public void beforeClass() {
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setPhoneNumber("081252930398");
		user.setPin("123456");

		register(user.getName(), user.getEmail(), user.getPhoneNumber(), user.getPin());
		checkStatusCode("200");

		login(user.getPhoneNumber());
		checkStatusCode("200");
		Map<String, String> data = response.getBody().jsonPath().getMap("data");
		user.setId(data.get("id"));
		
		verifyPinLogin(user.getId(), user.getPin());
		checkStatusCode("200");		
	}
	
	@BeforeMethod
	public void berforeMethod() {
		getCatalog(user.getSessionId(), transaction.getPhoneNumber());
		checkStatusCode("200");
	}
	
	@Test
	public void testCreateOrder() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();		
		createOrder(sessionId, transaction.getPhoneNumber(), transaction.getCatalog().getId());
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		
		if(code.equals("400")) {
			Assert.assertTrue(
					message.equals("selected catalog is not available for this phone’s provider") || 
					message.equals("invalid phone number")
					);
		} else if(code.equals("404")) {
			Assert.assertTrue(
					message.equals("catalog not found") || 
					message.equals("unknown phone number")
					);			
		} else if(code.equals("409")) {
			Assert.assertEquals(message, "you’ve already requested this exact order within the last 30 seconds, "
					+ "please try again later if you actually intended to do that"
					);
		} else if (code.equals("201")) {
			Assert.assertEquals(message, "created");			
		}
	}
	
	@Test(dependsOnMethods = {"testCreateOrder"})
	public void checkData() {
		String code = response.getBody().jsonPath().getString("code");
		
		if (code.equals("201")) {
			try {
				Connection conn = getConnectionOrder();
				String query = "SELECT A.value, A.price, B.id, B.name, B.image "
						+ "FROM pulsa_catalog A LEFT JOIN provider B on A.providerId = B.id "
						+ "WHERE A.id = ? ";
				
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setLong(1, Long.parseLong(transaction.getCatalog().getId()));
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					transaction.getCatalog().setValue(rs.getLong("value"));
					transaction.getCatalog().setPrice(rs.getLong("price"));
					transaction.getCatalog().getProvider().setId(rs.getString("id"));
					transaction.getCatalog().getProvider().setName(rs.getString("name"));
					transaction.getCatalog().getProvider().setImage(rs.getString("image"));
				}
				
				conn.close();
			} catch (SQLException e) {
				
			}
			
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");
			Assert.assertNotNull(data.get("id"));
			Assert.assertEquals(data.get("phone"), transaction.getPhoneNumber());
			Assert.assertEquals(data.get("catalog.id"), transaction.getCatalog().getId());
			Assert.assertEquals(data.get("catalog.provider"), transaction.getCatalog().getProvider());
			Assert.assertEquals(data.get("catalog.value"), transaction.getCatalog().getValue());
			Assert.assertEquals(data.get("catalog.price"), transaction.getCatalog().getPrice());
			
			transaction.setId((String) data.get("id"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = getConnectionOrder();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM transaction WHERE userId = ? ORDER BY createdAt DESC LIMIT 1");
			ps.setLong(1, Long.parseLong(user.getId()));
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Assert.assertEquals(rs.getString("id"), transaction.getId());
				Assert.assertEquals(rs.getString("phoneNumber"), transaction.getPhoneNumber());
				Assert.assertEquals(rs.getString("catalogId"), transaction.getCatalog().getId());
			}
			
			conn.close();
		} catch (SQLException e) {
			
		}
	}

	@AfterMethod
	public void afterMethod() {
		
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
