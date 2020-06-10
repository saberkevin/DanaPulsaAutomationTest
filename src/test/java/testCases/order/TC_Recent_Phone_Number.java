package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
	private User user;
	private String sessionId;
	private Provider[] providers;
	private String[] phoneNumbers;
	private String[] dateString;
	
	public TC_Recent_Phone_Number(String sessionId) {
		this.sessionId = sessionId;
	}
	
	private boolean isPhoneNumberRegexTrue(String phoneNumber) {
		String regex = "^(?=.[0-9])(?=.[a-z])(?=.[A-Z])(?=.[!@#$%^&*])(?=\\\\S+$).{8,}$";
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
	}
		
	@Test
	public void testRecentPhoneNumber() throws ParseException {
		if (sessionId.equals("true"))
			sessionId = user.getSessionId();
		getRecentPhoneNumber(sessionId);

		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		if(code.equals("200")) {
			String message = response.getBody().jsonPath().getString("message");
			Assert.assertEquals(message, "success");
		}
	}
	
	@Test(dependsOnMethods = {"testRecentPhoneNumber"})
	public void checkData() throws ParseException {
		if(!response.getBody().jsonPath().get("data").equals("[]")) {
			List<Map<String, String>> data = response.getBody().jsonPath().getList("data");	
			providers = new Provider[data.size()];
			phoneNumbers = new String[data.size()];
			dateString = new String[data.size()];
			
			for (int i = 0; i < data.size(); i++) {
				phoneNumbers[i] = data.get(i).get("number");
				Assert.assertTrue(isPhoneNumberRegexTrue(phoneNumbers[i]));

				providers[i].setId(data.get(i).get("provider.id"));
				providers[i].setName(data.get(i).get("provider.name"));
				providers[i].setImage(data.get(i).get("provider.image"));
				Assert.assertTrue(isProviderTrue(phoneNumbers[i], providers[i]));
				
				dateString[i] = data.get(i).get("date");
				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
				Assert.assertEquals(dateString[i], format.format(new Date()));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = getConnectionOrder();
			String query = "SELECT B.providerId, B.createdAt, A.phoneNumber "
					+ "FROM transaction A LEFT JOIN user B on A.userId = B.id "
					+ "WHERE B.id = ? "
					+ "ORDER BY A.createdAt DESC LIMIT 10";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setLong(1, Long.parseLong(user.getId()));
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Assert.assertEquals(rs.getString("providerId"), providers[rs.getRow()].getId());
				Assert.assertEquals(rs.getString("createdAt"), dateString[rs.getRow()]);
				Assert.assertEquals(rs.getString("phoneNumber"), phoneNumbers[rs.getRow()]);
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
