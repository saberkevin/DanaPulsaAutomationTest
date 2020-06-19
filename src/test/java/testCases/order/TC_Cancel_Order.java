package testCases.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.Catalog;
import model.Provider;
import model.Transaction;
import model.User;
import model.Voucher;

public class TC_Cancel_Order extends TestBase {
	private User user = new User();
	private String sessionId;
	private Transaction transaction = new Transaction();
	private Catalog catalog = new Catalog();
	private Provider provider = new Provider();
	private Voucher voucher = new Voucher();
	
	public TC_Cancel_Order(String sessionId, String transactionId) {
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

		getCatalog(user.getSessionId(), user.getUsername());
		checkStatusCode("200");
		
		createOrder(user.getSessionId(), user.getUsername(), transaction.getCatalogId());
		checkStatusCode("201");
	}
	
	@Test
	public void testCancelOrder() {
		if (sessionId.contentEquals("true"))
			sessionId = user.getSessionId();
		cancelOrder(sessionId, transaction.getId());
		
		String code = response.getBody().jsonPath().getString("code");
		checkStatusCode(code);
		
		String message = response.getBody().jsonPath().getString("message");
		
		if(code.equals("400")) {
			Assert.assertEquals(message, "can't cancel completed transaction");
		} else if(code.equals("404")) {
			Assert.assertEquals(message, "unknown transaction");
		} else if (code.equals("200")) {
			Assert.assertEquals(message, "deleted");
		}
	}
	
	@Test(dependsOnMethods = {"testCancelOrder"})
	public void checkData() {
		String code = response.getBody().jsonPath().getString("code");
		
		if (code.equals("200")) {
			try {
				Connection conn = setConnection("ORDER");
				String query = "SELECT A.userId, A.phoneNumber, A.createdAt, A.voucherId"
						+ "B.id [catalogId], B.value, B.price, "
						+ "C.id [providerId], C.name [providerName], C.image "
						+ "FROM transaction A LEFT JOIN pulsa_catalog B on A.catalogId = B.id "
						+ "LEFT JOIN provider C on B.providerId = C.id "
						+ "WHERE A.id = ?";
				
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setLong(1, transaction.getId());
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					transaction.setUserId(rs.getLong("userId"));
					transaction.setPhoneNumber(rs.getString("phoneNumber"));
					catalog.setId(rs.getLong("catalogId"));
					provider.setId(rs.getLong("providerId"));
					provider.setName(rs.getString("providerName"));
					provider.setImage(rs.getString("image"));
					catalog.setValue(rs.getLong("value"));
					catalog.setPrice(rs.getLong("price"));
					voucher.setId(rs.getLong("voucherId"));
					transaction.setStatus("CANCELED");
					transaction.setCreatedAt(rs.getDate("createdAt"));
					transaction.setUpdatedAt(new Date());
				}
				
				conn.close();
			} catch (SQLException e) {
				
			}
			
			try {
				Connection conn = setConnection("PROMOTION");
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM voucher WHERE A.id = ?");
				ps.setLong(1, voucher.getId());
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					voucher.setName(rs.getString("name"));
					voucher.setDiscount(rs.getLong("discount"));
					voucher.setMaxDeduction(rs.getLong("maxDeduction"));
				}
				
				conn.close();
			} catch (SQLException e) {
				
			}
			
			Map<String, String> data = response.getBody().jsonPath().getMap("data");
			
			Assert.assertEquals(data.get("id"), transaction.getId());
			Assert.assertEquals(data.get("phone"), transaction.getPhoneNumber());
			Assert.assertEquals(data.get("catalog.id"), catalog.getId());
			Assert.assertEquals(data.get("catalog.provider.id"), provider.getId());
			Assert.assertEquals(data.get("catalog.provider.name"), provider.getName());
			Assert.assertEquals(data.get("catalog.provider.image"), provider.getImage());
			Assert.assertEquals(data.get("catalog.value"), catalog.getValue());
			Assert.assertEquals(data.get("catalog.price"), catalog.getPrice());
			Assert.assertEquals(data.get("voucher.id"), voucher.getId());
			Assert.assertEquals(data.get("voucher.name"), voucher.getName());
			Assert.assertEquals(data.get("voucher.discount"), voucher.getDiscount());
			Assert.assertEquals(data.get("voucher.maxDeduction"), voucher.getMaxDeduction());
			Assert.assertEquals(data.get("method"), "E-Wallet");
			Assert.assertEquals(data.get("status"), transaction.getStatus());
			Assert.assertEquals(data.get("createdAt"), transaction.getCreatedAt());
			Assert.assertEquals(data.get("updatedAt"), transaction.getUpdatedAt());
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
