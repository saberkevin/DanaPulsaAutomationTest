package testCases.voucher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.User;
import model.Voucher;

public class TC_Promotion_Vouchers extends TestBase {
	private User user;
	private String sessionId;
	private String page;
	private JSONArray vouchers;
	
	public TC_Promotion_Vouchers(String sessionId, String page) {
		user = new User();
		this.sessionId = sessionId;
		this.page = page;
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
	public void testPromotionVouchers() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();
		getPromotionVoucher(sessionId, page);
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		
		if(code.equals("404")) {
			Assert.assertEquals(message, "There is no recommendation");
		} else if (code.equals("200")) {
			Assert.assertEquals(message, "success");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test(dependsOnMethods = {"testMyVouchers"})
	public void checkData() {
		String code = response.getBody().jsonPath().getString("code");
		
		if (code.equals("200")) {
			vouchers = (JSONArray) response.getBody().jsonPath().getList("data");
			
			Iterator<Voucher> itr = vouchers.iterator();
			while(itr.hasNext()) {
				Voucher voucher = (Voucher) itr.next();
				Assert.assertNotNull(voucher.getId());
				Assert.assertNotNull(voucher.getName());
				Assert.assertNotNull(voucher.getVoucherTypeName());
				Assert.assertNotNull(voucher.getDiscount());
				Assert.assertNotNull(voucher.getMaxDeduction());
				Assert.assertNotNull(voucher.getFilePath());
				Assert.assertNotNull(voucher.getExpiryDate());
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = setConnection("PROMOTION");
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM voucher");			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Assert.assertEquals(rs.getString("id"), ((Voucher) vouchers.get(rs.getRow())).getId());
				Assert.assertEquals(rs.getString("name"), ((Voucher) vouchers.get(rs.getRow())).getName());
				Assert.assertEquals(rs.getString("discount"), ((Voucher) vouchers.get(rs.getRow())).getDiscount());
				Assert.assertEquals(rs.getString("maxDeduction"), ((Voucher) vouchers.get(rs.getRow())).getMaxDeduction());
				Assert.assertEquals(rs.getString("filePath"), ((Voucher) vouchers.get(rs.getRow())).getFilePath());
				Assert.assertEquals(rs.getString("expiryDate"), ((Voucher) vouchers.get(rs.getRow())).getExpiryDate());
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
