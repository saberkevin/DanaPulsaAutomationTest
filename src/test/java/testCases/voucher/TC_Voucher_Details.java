package testCases.voucher;

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
import model.User;
import model.Voucher;

public class TC_Voucher_Details extends TestBase {
	private User user = new User();
	private String sessionId;
	private Voucher voucher = new Voucher();
	
	public TC_Voucher_Details(String sessionId, String voucherId) {
		this.sessionId = sessionId;
		voucher.setId(Long.parseLong(voucherId));
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
	}
	
	@Test
	public void testVoucherDetails() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();
		
		getVoucherDetails(sessionId, voucher.getId());
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		if (code.equals("200")) {
			String message = response.getBody().jsonPath().getString("message");
			Assert.assertTrue(message.equals("success") || message.equals("voucher not found"));
		}
	}
	
	@Test(dependsOnMethods = {"testMyVouchers"})
	public void checkData() {
		String code = response.getBody().jsonPath().getString("code");
		
		if (code.equals("200")) {
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");
			Assert.assertEquals(data.get("id"), voucher.getId());
			Assert.assertNotNull(data.get("name"));
			Assert.assertNotNull(data.get("filePath"));
			Assert.assertNotNull(data.get("voucherTypeName"));
			Assert.assertNotNull(data.get("paymentMethodName"));
			Assert.assertNotNull(data.get("discount"));
			Assert.assertNotNull(data.get("maxDeduction"));
			Assert.assertNotNull(data.get("minPurchase"));
			Assert.assertNotNull(data.get("expiryDate"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = setConnection("PROMOTION");
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM voucher WHERE id = ?");
			ps.setLong(1, voucher.getId());
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Assert.assertEquals(rs.getString("id"), voucher.getId());
				Assert.assertEquals(rs.getString("name"), voucher.getName());
				Assert.assertEquals(rs.getString("discount"), voucher.getDiscount());
				Assert.assertEquals(rs.getString("maxDeduction"), voucher.getMaxDeduction());
				Assert.assertEquals(rs.getString("filePath"), voucher.getFilePath());
				Assert.assertEquals(rs.getString("expiryDate"), voucher.getExpiryDate());
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
