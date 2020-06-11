package testCases.payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.Transaction;
import model.User;

public class TC_Pay_Order extends TestBase {
	private User user;
	private String sessionId;
	private Transaction transaction;
	private String paymentMethodId;

	public TC_Pay_Order(String sessionId, String transactionId, String paymentMethodId, String voucherId) {
		user = new User();
		transaction = new Transaction();
		this.sessionId = sessionId;
		transaction.setId(transactionId);
		this.paymentMethodId = paymentMethodId;
		transaction.getVoucher().setId(voucherId);
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
	
	@BeforeMethod
	public void berforeMethod() {
		getCatalog(user.getSessionId(), user.getPhoneNumber());
		checkStatusCode("200");
		
		createOrder(user.getSessionId(), user.getPhoneNumber(), transaction.getCatalog().getId());
		checkStatusCode("201");
	}
	
	@Test
	public void testPayOrder() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();
		payOrder(sessionId, transaction.getId(), paymentMethodId, transaction.getVoucher().getId());
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		
		if(code.equals("400")) {
			Assert.assertTrue(
					message.equals("not enough balance") || 
					message.equals("unknown method") || 
					message.equals("unknown voucher")
					);
		} else if(code.equals("404")) {
			Assert.assertTrue(
					message.equals("selected catalog is not available for this phone’s provider") || 
					message.equals("you don’t have any vouchers recommendation")
					);
		} else if (code.equals("202")) {
			Assert.assertEquals(message, "success");
		}
	}
	
	@Test(dependsOnMethods = {"testPayOrder"})
	public void checkData() {
		String code = response.getBody().jsonPath().getString("code");
		
		if (code.equals("200")) {
			try {
				Connection conn = getConnectionOrder();
				String query = "SELECT A.userId, A.phoneNumber, A.createdAt, A.updatedAt, "
						+ "B.id [catalogId], B.value, B.price, "
						+ "C.id [providerId], C.name [providerName], C.image, "
						+ "D.name [paymentMethod], "
						+ "E.id [voucherId], E.name [voucherName], E.discount, E.maxDeduction "
						+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
						+ "LEFT JOIN provider C on B.providerId = C.id "
						+ "LEFT JOIN paymentMethod D on A.methodId = D.id "
						+ "LEFT JOIN voucher E on A.voucherId = F.id "
						+ "WHERE A.id = ?";
				
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setLong(1, Long.parseLong(transaction.getId()));
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					transaction.setUserId(rs.getString("userId"));
					transaction.setPhoneNumber(rs.getString("phoneNumber"));
					transaction.getCatalog().setId(rs.getString("catalogId"));
					transaction.getCatalog().getProvider().setId(rs.getString("providerId"));
					transaction.getCatalog().getProvider().setName(rs.getString("providerName"));
					transaction.getCatalog().getProvider().setImage(rs.getString("image"));
					transaction.getCatalog().setValue(rs.getLong("value"));
					transaction.getCatalog().setPrice(rs.getLong("price"));
					transaction.getVoucher().setId(rs.getString("voucherId"));
					transaction.getVoucher().setName(rs.getString("voucherName"));
					transaction.getVoucher().setDiscount(rs.getLong("discount"));
					transaction.getVoucher().setMaxDeduction(rs.getLong("maxDeduction"));
					transaction.setPaymentMethod(rs.getString("paymentMethod"));
					transaction.setStatus("WAITING");
					transaction.setCreatedAt(rs.getDate("createdAt"));
					transaction.setUpdatedAt(rs.getDate("updatedAt"));
				}
				
				conn.close();
			} catch (SQLException e) {
				
			}
			
			JSONObject data = response.getBody().jsonPath().getJsonObject("data");
			Assert.assertEquals(data.get("id"), transaction.getId());
			Assert.assertEquals(data.get("phone"), user.getPhoneNumber());
			Assert.assertEquals(data.get("catalog.id"), transaction.getCatalog().getId());
			Assert.assertEquals(data.get("catalog.provider"), transaction.getCatalog().getProvider());
			Assert.assertEquals(data.get("catalog.value"), transaction.getCatalog().getValue());
			Assert.assertEquals(data.get("catalog.price"), transaction.getCatalog().getPrice());
			Assert.assertEquals(data.get("voucher.id"), transaction.getVoucher().getId());
			Assert.assertEquals(data.get("voucher.name"), transaction.getVoucher().getName());
			Assert.assertEquals(data.get("voucher.deduction"), transaction.getVoucher().getDiscount());
			Assert.assertEquals(data.get("voucher.maxDeduction"), transaction.getVoucher().getMaxDeduction());
			Assert.assertEquals(data.get("method"), transaction.getPaymentMethod());
			Assert.assertEquals(data.get("status"), transaction.getStatus());
			Assert.assertEquals(data.get("createdAt"), transaction.getCreatedAt());
			Assert.assertEquals(data.get("updatedAt"), transaction.getUpdatedAt());
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		try {
			Connection conn = getConnectionOrder();
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM transaction WHERE id = ?");
			ps.setLong(1, Long.parseLong(transaction.getId()));
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Assert.assertEquals(rs.getString("userId"), user.getId());
				Assert.assertEquals(rs.getString("phoneNumber"), transaction.getPhoneNumber());
				Assert.assertEquals(rs.getString("catalogId"), transaction.getCatalog().getId());
			}
			
			conn.close();
		} catch (SQLException e) {
			
		}
	}

	@AfterMethod
	public void afterMethod() {
		
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());				
	}
}
