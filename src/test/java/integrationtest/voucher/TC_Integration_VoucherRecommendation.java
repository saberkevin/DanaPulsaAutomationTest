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
import model.Catalog;
import model.Provider;
import model.Transaction;
import model.User;

public class TC_Integration_VoucherRecommendation extends TestBase {
	private User user = new User();
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private String testCase;
	private String transactionId;
	private String result;
	
	public TC_Integration_VoucherRecommendation() {
		
	}
	
	public TC_Integration_VoucherRecommendation(String testCase, String transactionId, String result) {
		this.testCase = testCase;
		this.transactionId = transactionId;
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
		
		// get catalog TELKOMSEL 30K
		getCatalog(user.getSessionId(), user.getUsername().substring(0,6));
		checkStatusCode("200");
		user.setSessionId(response.getCookie("JSESSIONID"));
		List<Map<String, String>> vouchers = response.getBody().jsonPath().getList("data.catalog");
		catalog.setId(Long.parseLong(vouchers.get(3).get("id")));

		// create order
		createOrder(user.getSessionId(), user.getUsername(), Long.toString(catalog.getId()));
		checkStatusCode("201");
		user.setSessionId(response.getCookie("JSESSIONID"));
		transaction.setId(response.getBody().jsonPath().getLong("data.id"));
		
		// insert voucher into database
		createUserVoucher(user.getId(), 4, 2);
	}
	
	@Test
	public void testRecommendationVouchers() {	
		getRecommendationVoucher(user.getSessionId(), transactionId);
		checkStatusCode("200");

		Assert.assertTrue(response.getBody().asString().contains(result));

		Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
		Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
	}
	
	@Test(dependsOnMethods = {"testRecommendationVouchers"})
	public void checkData() {
		if (!response.getBody().jsonPath().getString("data").equals("[]")) {
			List<Map<String, String>> vouchers = response.jsonPath().getList("data");
		
			Assert.assertTrue(vouchers.size() <= 10, "maximum vouchers is 10");
			
			for (int i = 0; i < vouchers.size(); i++) {
				Assert.assertNotNull(vouchers.get(i).get("id"));
				Assert.assertNotNull(vouchers.get(i).get("name"));
				Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
				Assert.assertNotNull(vouchers.get(i).get("discount"));
				Assert.assertNotNull(vouchers.get(i).get("maxDeduction"));
				Assert.assertNotNull(vouchers.get(i).get("value"));
				Assert.assertNotNull(vouchers.get(i).get("filePath"));
				Assert.assertNotNull(vouchers.get(i).get("expiryDate"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		if (!response.getBody().jsonPath().getString("data").equals("[]")) {		
			List<Map<String, String>> vouchers = response.jsonPath().get();
			
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT "
						+ "A.id, "
						+ "A.name, "
						+ "D.name AS voucherTypeName, "
						+ "A.value, "
						+ "A.discount, "
						+ "A.maxDeduction, "
						+ "A.filePath, "
						+ "A.expiryDate "
						+ "FROM voucher AS A "
						+ "JOIN user_voucher AS B ON B.voucherId = A.id "
						+ "JOIN user_voucher_status AS C ON B.voucherStatusId = C.id "
						+ "JOIN voucher_type AS D ON D.id = A.typeId "
						+ "JOIN voucher_provider AS E ON E.voucherId = A.id "
						+ "JOIN issue_voucher_rule AS F ON F.voucherId = A.id "
						+ "JOIN voucher_payment_method AS G ON G.voucherId = A.id "
						+ "WHERE B.userId = ? AND B.voucherStatusId != 1 AND G.paymentMethodId = 1 "
						+ "AND E.providerId = ? AND F.minPurchase <= ? ORDER BY A.maxDeduction DESC";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, user.getId());
				ps.setLong(2, provider.getId());
				ps.setLong(3, catalog.getPrice());
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no voucher found in database");
				}
				do {
					int index = rs.getRow() - 1;
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("id")), rs.getString("id"));
					Assert.assertEquals(vouchers.get(index).get("name"), rs.getString("name"));
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("discount")), rs.getString("discount"));
					Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), rs.getString("voucherTypeName"));
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("maxDeduction")), rs.getString("maxDeduction"));
					Assert.assertEquals(String.valueOf(vouchers.get(index).get("value")), rs.getString("value"));
					Assert.assertEquals(vouchers.get(index).get("filePath"), rs.getString("filePath"));
//					Assert.assertEquals(vouchers.get(index).get("expiryDate"), rs.getLong("expiryDate"));
				} while (rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		// logout
		logout(user.getSessionId());
		checkStatusCode("200");
		
		// delete user
		deleteTransactionByUserId(user.getId());
		deleteUserVoucherByUserId(user.getId());
		deleteBalanceByUserId(user.getId());
		deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		
		// tear down test case
		tearDown("Finished " + this.getClass().getSimpleName());		
	}
}
