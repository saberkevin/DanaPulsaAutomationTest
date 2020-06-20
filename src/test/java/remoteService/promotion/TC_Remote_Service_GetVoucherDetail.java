package remoteService.promotion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		
		final String errorMessage1 = "voucher not found";
		final String errorMessage2 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else {
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
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "voucher not found";
		final String errorMessage2 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM voucher WHERE id = ?";
			param.put("1", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;		
		case errorMessage2:
			// do some code
			break;
		default:
			query = "SELECT A.*, B.name AS voucherTypeName FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id WHERE A.id = ?";
			param.put("1", Long.parseLong(voucherId));
			data = sqlExec(query, param, "PROMOTION");
			
			Map<String, Object>  voucher = response.getBody().jsonPath().get();

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(voucher.get("id"), map.get("id"));
				Assert.assertEquals(voucher.get("name"), map.get("name"));
				Assert.assertEquals(voucher.get("discount"), map.get("discount"));
				Assert.assertEquals(voucher.get("voucherTypeName"), map.get("voucherTypeName"));
				Assert.assertEquals(Long.valueOf((Integer) voucher.get("minPurchase")), map.get("minPurchase"));
				Assert.assertEquals(Long.valueOf((Integer) voucher.get("maxDeduction")), map.get("maxDeduction"));
				Assert.assertEquals(voucher.get("value"), map.get("value"));
				Assert.assertEquals(voucher.get("filePath"), map.get("filePath"));

				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				Assert.assertEquals(formatter.format(voucher.get("expiryDate")), formatter.format(map.get("expiryDate")));
				Assert.assertEquals(voucher.get("active"), map.get("isActive"));
			}
			
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
