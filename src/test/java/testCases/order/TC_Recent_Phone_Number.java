package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.Provider;
import model.User;

public class TC_Recent_Phone_Number extends TestBase {
	private User user = new User();
	private Provider[] providers;
	private String[] phoneNumbers;
	private String[] dateString;
	private String sessionId;
	
	public TC_Recent_Phone_Number(String sessionId) {
		this.sessionId = sessionId;
	}
	
	private boolean isPhoneNumberRegexTrue(String phoneNumber) {
		String regex = "^08[0-9]{9,13}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(phoneNumber);
		return matcher.matches();
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
			e.printStackTrace();
		}
		return false;
	}
	
	@BeforeClass
	public void beforeClass() {
		// initialize user
		user.setName("Zanuar");
		user.setEmail("triromadon@gmail.com");
		user.setUsername("081252930398");
		user.setPin(123456);
		
		// insert user into database and get user id from it
		deleteUserIfExist(user.getEmail(), user.getUsername());
		createUser(user);
		user.setId(getUserIdByUsername(user.getUsername()));
		System.out.println(user.getId());

		// get session from mobile domain - API verify pin login
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		user.setSessionId(response.getCookie("SESSION"));	

		System.out.println(response.getBody().asString());
		Assert.assertTrue(response.getStatusCode() == 200);

		// if data from excel "true", then get valid session
		if (user.getSessionId().equals("true")) {
			sessionId = user.getSessionId();
		}
		
		// insert transaction into database - Telkomsel 15k
		createTransaction(user.getId(), user.getUsername(), 13);
	}
		
	@Test
	public void testRecentPhoneNumber() throws ParseException {
		// call API recent phone number
		getRecentPhoneNumber(sessionId);		
		System.out.println(response.getBody().asString());

		if (response.getStatusCode() == 401) {
			Assert.assertTrue(response.getBody().jsonPath().getInt("status") == 401);
			Assert.assertTrue(response.getBody().jsonPath().getString("error").equals("Unauthorized"));
			Assert.assertTrue(response.getBody().jsonPath().getString("message").equals(""));
			Assert.assertTrue(response.getBody().jsonPath().getString("path").equals("/api/recent-number"));
		} else {
			// compare code with HTTP status code
			String code = response.getBody().jsonPath().getString("code");
			checkStatusCode(code);
			
			// check message
			if(code.equals("200")) {
				String message = response.getBody().jsonPath().getString("message");
				Assert.assertEquals(message, "success");
			}
		}
	}
	
	@Test(dependsOnMethods = {"testRecentPhoneNumber"})
	public void checkData() throws ParseException {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			if(!response.getBody().jsonPath().get("data").equals("[]")) {
				// convert "data" to list
				List<Map<String, String>> data = response.getBody().jsonPath().getList("data");	
				
				// allocating memory to array by data size
				providers = new Provider[data.size()];
				phoneNumbers = new String[data.size()];
				dateString = new String[data.size()];
				
				for (int i = 0; i < data.size(); i++) {
					// get phone number
					phoneNumbers[i] = data.get(i).get("number");
					Assert.assertTrue(isPhoneNumberRegexTrue(phoneNumbers[i]));
	
					// get provider
					providers[i].setId(Long.parseLong(data.get(i).get("provider.id")));
					providers[i].setName(data.get(i).get("provider.name"));
					providers[i].setImage(data.get(i).get("provider.image"));
					Assert.assertTrue(isProviderTrue(phoneNumbers[i], providers[i]));
					
					// get date
					dateString[i] = data.get(i).get("date");
					Assert.assertNotNull(dateString[i]);
				}
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			// check recent phone number with data from transaction table in database
			try {
				Connection conn = getConnectionOrder();
				String query = "SELECT * FROM transaction WHERE userId = ? ORDER BY createdAt DESC LIMIT 10";
	
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setLong(1, user.getId());
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					Assert.assertEquals(rs.getString("phoneNumber"), phoneNumbers[rs.getRow()]);
					Assert.assertEquals(rs.getString("providerId"), providers[rs.getRow()].getId());
					Assert.assertEquals(rs.getString("createdAt"), dateString[rs.getRow()]);
				}
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// check each of providers with data from provider table in database
			for (int i = 0; i < providers.length; i++) {
				try {
					Connection conn = getConnectionOrder();
					String query = "SELECT * FROM provider WHERE id = ?";
		
					PreparedStatement ps = conn.prepareStatement(query);
					ps.setLong(1, providers[i].getId());
					
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						Assert.assertEquals(rs.getString("name"), providers[rs.getRow()].getName());
						Assert.assertEquals(rs.getString("image"), providers[rs.getRow()].getImage());
					}
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		// delete user's transaction
		deleteTransactionByUserId(user.getId());
		
		// logout (destroy session)
		logout(user.getSessionId());

		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
