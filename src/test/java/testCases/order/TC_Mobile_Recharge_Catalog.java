package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.Catalog;
import model.Provider;
import model.User;

public class TC_Mobile_Recharge_Catalog extends TestBase {
	private User user;
	private String sessionId;
	private String phoneNumber;
	private Provider provider;
	private JSONArray catalogs;
	
	public TC_Mobile_Recharge_Catalog(String sessionId, String phoneNumber) {
		user = new User();
		this.sessionId = sessionId;
		this.phoneNumber = phoneNumber;
	}
	
	private boolean isProviderTrue(String phoneNumber, Provider provider) {
		try {
			Connection conn = getConnectionOrder();
			String query = "SELECT name FROM provider WHERE id = (SELECT providerId FROM provider_prefix WHERE prefix = ?";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, phoneNumber.substring(0,5));
			
			ResultSet rs = ps.executeQuery();
			if (rs.getString("name").equals(provider.getName()))
				return true;
			
			conn.close();
		} catch (SQLException e) {
			
		}
		return false;
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
		user.setSessionId(response.getHeader("Cookie"));
	}
	
	@Test
	public void testMobileRechargeCatalog() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();	
		getCatalog(sessionId, phoneNumber);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);

		String message = response.getBody().jsonPath().getString("message");
		
		if (code.equals("400")) {
			Assert.assertEquals(message, "invalid phone number");
		} else if(code.equals("404")) {
			Assert.assertEquals(message, "unknown phone number");
		} else if(code.equals("200")) {
			Assert.assertEquals(message, "success");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"testMobileRechargeCatalog"})
	public void checkData() {
		String code = response.getBody().jsonPath().getString("code");
		
		if (code.equals("200")) {
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");

			provider = (Provider) data.get("provider");					
			Assert.assertTrue(isProviderTrue(phoneNumber, provider));
			
			catalogs = (JSONArray) data.get("catalog");
			
			Iterator<Catalog> itr = catalogs.iterator();
			while(itr.hasNext()) {
				Catalog catalog = itr.next();
				Assert.assertNotNull(catalog.getId());
				Assert.assertNotNull(catalog.getValue());
				Assert.assertNotNull(catalog.getPrice());				
			}				
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = getConnectionOrder();
			String query = "SELECT * FROM pulsa_catalog WHERE providerId = ? ORDER BY value DESC";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setLong(1, Long.parseLong(provider.getId()));
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Assert.assertEquals(rs.getString("id"), ((Catalog) catalogs.get(rs.getRow())).getId());
				Assert.assertEquals(rs.getLong("value"), ((Catalog) catalogs.get(rs.getRow())).getValue());
				Assert.assertEquals(rs.getLong("price"), ((Catalog) catalogs.get(rs.getRow())).getPrice());
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
