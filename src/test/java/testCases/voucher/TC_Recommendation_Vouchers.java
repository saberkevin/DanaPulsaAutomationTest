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
import model.Transaction;
import model.User;
import model.Voucher;

public class TC_Recommendation_Vouchers extends TestBase {
	private User user = new User();
	private String sessionId;
	private Transaction transaction = new Transaction();
	private JSONArray vouchers;
	
	public TC_Recommendation_Vouchers(String sessionId, String transactionId) {
		this.sessionId = sessionId;
		transaction.setId(Long.parseLong(transactionId));
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
	public void testRecommendationVouchers() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();		
		getRecommendationVoucher(sessionId, Long.toString(transaction.getId()));
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		
		if(code.equals("404")) {
			Assert.assertEquals(message, "you don’t have any vouchers recommendation");
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
			Connection conn = getConnectionPromotion();
			String query = "SELECT A.* FROM "
					+ "voucher A LEFT JOIN user_voucher B on A.id = B.voucherId "
					+ "WHERE userId = ?";
			
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setLong(1, user.getId());
			
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
