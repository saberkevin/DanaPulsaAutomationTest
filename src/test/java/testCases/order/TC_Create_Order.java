package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.Catalog;
import model.Provider;
import model.Transaction;
import model.User;

public class TC_Create_Order extends TestBase {
	private User user = new User();
	private String sessionId;
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	
	public TC_Create_Order(String sessionId, String phoneNumber, String catalogId) {
		this.sessionId = sessionId;
		transaction.setPhoneNumber(phoneNumber);
		transaction.setCatalogId(Long.parseLong(catalogId));
	}
	
	@BeforeClass
	public void beforeClass() {
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setUsername("081252930398");
		user.setPin(123456);
		
		deleteUserIfExist(user.getEmail(), user.getUsername());
		createUser(user);
		user.setId(getUserIdByUsername(user.getUsername()));
		
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getHeader("Cookie"));

		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getHeader("Cookie"));

		getCatalog(user.getSessionId(), transaction.getPhoneNumber());
		checkStatusCode("200");
	}
	
	@Test
	public void testCreateOrder() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();		
		createOrder(sessionId, transaction.getPhoneNumber(), transaction.getCatalogId());
		
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
				Connection conn = setConnection("ORDER");
				String query = "SELECT A.id [providerId], A.name, A.image, "
						+ "B.id [catalogId], B.value, B.price "
						+ "FROM provider A LEFT JOIN pulsa_catalog B on A.id = B.providerId "
						+ "WHERE B.id = ? ";
				
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setLong(1, transaction.getCatalogId());
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					provider.setId(rs.getLong("providerId"));
					provider.setName(rs.getString("name"));
					provider.setImage(rs.getString("image"));
					catalog.setId(rs.getLong("catalogId"));
					catalog.setValue(rs.getLong("value"));
					catalog.setPrice(rs.getLong("price"));					
				}
				
				conn.close();
			} catch (SQLException e) {
				
			}
			
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");
			Assert.assertNotNull(data.get("id"));
			Assert.assertEquals(data.get("phone"), transaction.getPhoneNumber());
			Assert.assertEquals(data.get("catalog.id"), catalog.getId());
			Assert.assertEquals(data.get("catalog.provider.id"), provider.getId());
			Assert.assertEquals(data.get("catalog.provider.name"), provider.getName());
			Assert.assertEquals(data.get("catalog.provider.image"), provider.getImage());
			Assert.assertEquals(data.get("catalog.value"), catalog.getValue());
			Assert.assertEquals(data.get("catalog.price"), catalog.getPrice());
			
			transaction.setId((Long) data.get("id"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = setConnection("ORDER");
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM transaction WHERE id = ?");
			ps.setLong(1, transaction.getId());
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Assert.assertEquals(rs.getString("userId"), user.getId());
				Assert.assertEquals(rs.getString("phoneNumber"), transaction.getPhoneNumber());
				Assert.assertEquals(rs.getString("catalogId"), catalog.getId());
			}
			
			conn.close();
		} catch (SQLException e) {
			
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
