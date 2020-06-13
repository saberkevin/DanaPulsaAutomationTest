package remoteService.promotion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.RestAssured;
import io.restassured.http.Method;

public class TC_Remote_Service_GetVoucherDetail extends TestBase {
	private String voucherId;

	public TC_Remote_Service_GetVoucherDetail(String voucherId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
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
	
	@Test
	public void testVoucherDetails() {
		// call API get voucher details
		getVoucherDetailsRemoteService(voucherId);
	}
	
	@Test(dependsOnMethods = {"testVoucherDetails"})
	public void checkData() throws ParseException {
		
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			List<Map<String, String>> vouchers = response.jsonPath().get();
			
			if(vouchers != null) {
				try {
					Connection conn = getConnectionPromotion();
					String queryString = "SELECT A.*, B.name AS voucherTypeName "
							+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
							+ "WHERE A.id = ?";
					
					PreparedStatement ps = conn.prepareStatement(queryString);
					ps.setLong(1, Long.parseLong(voucherId));
					
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						int index = rs.getRow() - 1;
						Assert.assertEquals(vouchers.get(index).get("name"), rs.getString("name"));
						Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), rs.getString("voucherTypeName"));
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("discount")), rs.getString("discount"));
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("minPurchase")), rs.getString("minPurchase"));
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("maxDeduction")), rs.getString("maxDeduction"));
						Assert.assertEquals(String.valueOf(vouchers.get(index).get("value")), rs.getString("value"));
						Assert.assertEquals(vouchers.get(index).get("filePath"), rs.getString("filePath"));
//						Assert.assertEquals(vouchers.get(index).get("expiryDate"), rs.getLong("expiryDate"));
//						Assert.assertEquals(vouchers.get(index).get("active"), rs.getString("isActive"));
					}
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else if (statusCode == 400) {
				Assert.assertEquals(response.getBody().jsonPath().getString("code"), "400");
				Assert.assertEquals(response.getBody().jsonPath().getString("message"), "voucher not found");
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
