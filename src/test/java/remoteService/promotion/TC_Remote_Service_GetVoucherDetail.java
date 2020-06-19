package remoteService.promotion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;

public class TC_Remote_Service_GetVoucherDetail extends TestBase {
	private String testCase;
	private String voucherId;
	private String result;

	public TC_Remote_Service_GetVoucherDetail() {
	}

	public TC_Remote_Service_GetVoucherDetail(String testCase, String voucherId, String result) {
		this.testCase = testCase;
		this.voucherId = voucherId;
		this.result = result;
	}

	@SuppressWarnings("unchecked")
	public void getVoucherDetailsRemoteService(String voucherId) {
		logger.info("Call Get Voucher Details API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("voucher id:" + voucherId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", ConfigRemoteServicePromotion.QUEUE_GET_VOUCHER_DETAILS);
		requestParams.put("request", voucherId);
		
		RestAssured.baseURI = ConfigRemoteServicePromotion.BASE_URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, ConfigRemoteServicePromotion.ENDPOINT_PATH);
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void BeforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
	}
	
	@Test
	public void testVoucherDetails() {
		getVoucherDetailsRemoteService(voucherId);

		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testVoucherDetails"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result), responseBody);
		
		if (!responseBody.equals("voucher not found") && !responseBody.equals("invalid request format")) {
			Assert.assertEquals(Integer.toString(response.body().jsonPath().get("id")), voucherId);
			Assert.assertNotNull(response.getBody().jsonPath().get("name"));
			Assert.assertNotNull(response.getBody().jsonPath().get("discount"));
			Assert.assertNotNull(response.getBody().jsonPath().get("voucherTypeName"));
			Assert.assertNotNull(response.getBody().jsonPath().get("minPurchase"));
			Assert.assertNotNull(response.getBody().jsonPath().get("maxDeduction"));
			Assert.assertNotNull(response.getBody().jsonPath().get("filePath"));
			Assert.assertNotNull(response.getBody().jsonPath().get("expiryDate"));
			Assert.assertNotNull(response.getBody().jsonPath().get("active"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("voucher not found")) {
			try {
				Connection conn = getConnectionPromotion();
				String queryString = "SELECT * FROM voucher WHERE id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(voucherId));
				
				ResultSet rs = ps.executeQuery();
				Assert.assertTrue(!rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (responseBody.equals("invalid request format")) {
			// do some code
			
		} else {
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
					Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("id")), rs.getString("id"));
					Assert.assertEquals(response.getBody().jsonPath().get("name"), rs.getString("name"));
					Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("discount")), rs.getString("discount"));
					Assert.assertEquals(response.getBody().jsonPath().get("voucherTypeName"), rs.getString("voucherTypeName"));
					Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("minPurchase")), rs.getString("minPurchase"));
					Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("maxDeduction")), rs.getString("maxDeduction"));
					Assert.assertEquals(Integer.toString(response.getBody().jsonPath().get("value")), rs.getString("value"));
					Assert.assertEquals(response.getBody().jsonPath().get("filePath"), rs.getString("filePath"));
//						Assert.assertEquals(response.getBody().jsonPath().get("expiryDate"), rs.getString("expiryDate"));
//						Assert.assertEquals(response.getBody().jsonPath().get("active"), rs.getString("active"));
				} while (rs.next());
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
