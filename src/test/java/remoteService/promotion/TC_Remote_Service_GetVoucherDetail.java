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
	private String description;
	private String voucherId;

	public TC_Remote_Service_GetVoucherDetail() {
	}

	public TC_Remote_Service_GetVoucherDetail(String description, String voucherId) {
		this.description = description;
		this.voucherId = voucherId;
	}

	@SuppressWarnings("unchecked")
	public void getVoucherDetailsRemoteService(String voucherId) {
		logger.info("Call Get Voucher Details API [Promotion Domain]");
		logger.info("Test Data: ");
		logger.info("voucher id:" + voucherId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("queue", "getVoucherDetail");
		requestParams.put("request", voucherId);
		
		RestAssured.baseURI = URIPromotion;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, "/test");
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void BeforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + description);
	}
	
	@Test
	public void testVoucherDetails() {
		// call API get voucher details
		getVoucherDetailsRemoteService(voucherId);
		
		int statusCode = response.getStatusCode();

		if (statusCode != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testVoucherDetails"})
	public void checkData() throws ParseException {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			String responseBody = response.getBody().asString();
			
			if (!responseBody.contains("Unexpected") 
					&& !responseBody.equals("voucher not found")
					&& !responseBody.equals("For input string: \"\"")) {
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
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
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
			} else if (responseBody.contains("Unexpected")) {
				// do some code
				
			} else if (responseBody.equals("For input string: \"\"")) {
				// do some code
				
			} else {
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT A.*, B.name AS voucherTypeName "
							+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
							+ "WHERE A.id = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, Long.parseLong(voucherId));
					
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
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
					}
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
