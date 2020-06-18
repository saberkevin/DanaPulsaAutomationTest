package integrationtest.voucher;

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
import model.User;

public class TC_Integration_VoucherDetails extends TestBase {
	private User user = new User();
	private String testCase;
	private String voucherId;
	private String result;
	
	public TC_Integration_VoucherDetails() {
		
	}
	
	public TC_Integration_VoucherDetails(String testCase, String voucherId, String result) {
		this.testCase = testCase;
		this.voucherId = voucherId;
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
		
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		// get voucher id from promotion
		getPromotionVoucher(user.getSessionId(), "1");
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		List<Map<String, String>> vouchers = response.getBody().jsonPath().getList("data");
		voucherId = String.valueOf(vouchers.get(Integer.parseInt(voucherId) - 1).get("id"));
	}
	
	@Test
	public void testVoucherDetails() {		
		getVoucherDetails(user.getSessionId(), voucherId);
		checkStatusCode("200");
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
	}
	
	@Test(dependsOnMethods = {"testVoucherDetails"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			Map<String, String> data = response.getBody().jsonPath().get("data");
			
			Assert.assertEquals(String.valueOf(data.get("id")), voucherId);
			Assert.assertNotNull(data.get("name"));
			Assert.assertNotNull(data.get("voucherTypeName"));
			Assert.assertNotNull(data.get("discount"));
			Assert.assertNotNull(data.get("minPurchase"));
			Assert.assertNotNull(data.get("maxDeduction"));
			Assert.assertNotNull(data.get("value"));
			Assert.assertNotNull(data.get("filePath"));
			Assert.assertNotNull(data.get("expiryDate"));
			Assert.assertNotNull(data.get("active"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			Map<String, String> data = response.getBody().jsonPath().get("data");
			
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.*, "
						+ "B.name AS voucherTypeName "
						+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
						+ "WHERE A.id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(voucherId));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no voucher found in database");
				}
				do {
					Assert.assertEquals(String.valueOf(data.get("id")), rs.getString("id"));
					Assert.assertEquals(data.get("name"), rs.getString("name"));
					Assert.assertEquals(String.valueOf(data.get("discount")), rs.getString("discount"));
					Assert.assertEquals(data.get("voucherTypeName"), rs.getString("voucherTypeName"));
					Assert.assertEquals(String.valueOf(data.get("minPurchase")), rs.getString("minPurchase"));
					Assert.assertEquals(String.valueOf(data.get("maxDeduction")), rs.getString("maxDeduction"));
					Assert.assertEquals(String.valueOf(data.get("value")), rs.getString("value"));
					Assert.assertEquals(data.get("filePath"), rs.getString("filePath"));
//					Assert.assertEquals(data.get("expiryDate"), rs.getString("expiryDate"));
//					Assert.assertEquals(data.get("active"), rs.getString("active"));
				} while (rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		// delete user
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
