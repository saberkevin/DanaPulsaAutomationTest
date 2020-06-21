package remoteService.promotion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import io.restassured.path.json.JsonPath;

public class TC_Remote_Service_GetVoucherDetail extends TestBase {
	private String testCase;
	private String voucherId;
	private String result;
	private String dataAMQP;
	private JsonPath responseData;

	public TC_Remote_Service_GetVoucherDetail(String testCase, String voucherId, String result) {
		this.testCase = testCase;
		this.voucherId = voucherId;
		this.result = result;
	}
	
	@BeforeClass
	public void BeforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
	}
	
	@Test
	public void testVoucherDetails() {
		dataAMQP = callRP(promotionAMQP, ConfigRemoteServicePromotion.QUEUE_GET_VOUCHER_DETAILS, voucherId);
		responseData = new JsonPath(dataAMQP);
		logger.info("message = " + voucherId);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testVoucherDetails"})
	public void checkData() throws ParseException {
		Assert.assertTrue(dataAMQP.contains(result), dataAMQP);
		
		final String errorMessage1 = "voucher not found";
		final String errorMessage2 = "invalid request format";
		
		if (dataAMQP.contains(errorMessage1)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage2)) {
			// do some code
		} else {
			Assert.assertEquals(Integer.toString(responseData.get("id")), voucherId);
			Assert.assertNotNull(responseData.get("name"));
			Assert.assertNotNull(responseData.get("discount"));
			Assert.assertNotNull(responseData.get("voucherTypeName"));
			Assert.assertNotNull(responseData.get("minPurchase"));
			Assert.assertNotNull(responseData.get("maxDeduction"));
			Assert.assertNotNull(responseData.get("filePath"));
			Assert.assertNotNull(responseData.get("expiryDate"));
			Assert.assertNotNull(responseData.get("active"));
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "voucher not found";
		final String errorMessage2 = "invalid request format";

		switch (dataAMQP) {
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
			
			Map<String, Object>  voucher = responseData.get();

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
