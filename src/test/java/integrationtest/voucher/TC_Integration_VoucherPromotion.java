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

public class TC_Integration_VoucherPromotion extends TestBase {
	private User user = new User();
	private String testCase;
	private String page;
	private String result;
	
	public TC_Integration_VoucherPromotion() {
		
	}
	
	public TC_Integration_VoucherPromotion(String testCase, String page, String result) {
		this.testCase = testCase;
		this.page = page;
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
		
		// verify pin login
		verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
	}
	
	@Test
	public void testPromotionVouchers() {
		getPromotionVoucher(user.getSessionId(), page);
		checkStatusCode("200");
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
	}
	
	@Test(dependsOnMethods = {"testPromotionVouchers"})
	public void checkData() {
		if (!response.getBody().jsonPath().getString("data").equals("[]")) {
			List<Map<String, String>> vouchers = response.getBody().jsonPath().getList("data");
			
			Assert.assertTrue(vouchers.size() <= 10, "maximum vouchers per page is 10");
			
			for (int i = 0; i < vouchers.size(); i++) {
				Assert.assertNotNull(vouchers.get(i).get("id"));
				Assert.assertNotNull(vouchers.get(i).get("name"));
				Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
				Assert.assertNotNull(vouchers.get(i).get("filePath"));
				Assert.assertNotNull(vouchers.get(i).get("expiryDate"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String dataString = response.getBody().jsonPath().getString("data");
			
		if (dataString.equals("[]")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.id, "
						+ "A.name AS voucherName, "
						+ "B.name AS voucherTypeName, "
						+ "A.filePath, "
						+ "A.expiryDate "
						+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
						+ "WHERE A.isActive = 1 AND A.id NOT IN (SELECT voucherId FROM user_voucher where userId = ? AND voucherStatusId = 2) "
						+ "ORDER BY A.id ASC LIMIT ?, 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setInt(2, (Integer.parseInt(page)-1) * 10);
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				
			}				
		} else {
			List<Map<String, String>> vouchers = response.getBody().jsonPath().getList("data");
			
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.id, "
						+ "A.name AS voucherName, "
						+ "B.name AS voucherTypeName, "
						+ "A.filePath, "
						+ "A.expiryDate "
						+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
						+ "WHERE A.isActive = 1 AND A.id NOT IN (SELECT voucherId FROM user_voucher where userId = ? AND voucherStatusId = 2) "
						+ "ORDER BY A.id ASC LIMIT ?, 10";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setInt(2, (Integer.parseInt(page)-1) * 10);
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no vouchers found in database");
				}
				do {
					int index = rs.getRow() - 1;
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("id")), rs.getString("id"));
					Assert.assertEquals(vouchers.get(index).get("name"), rs.getString("voucherName"));						
					Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), rs.getString("voucherTypeName"));						
					Assert.assertEquals(vouchers.get(index).get("filePath"), rs.getString("filePath"));						
//						Assert.assertEquals(vouchers.get(index).get("expiryDate"), rs.getLong("expiryDate"));
				} while(rs.next());
				
				conn.close();
			} catch (SQLException e) {
				
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
