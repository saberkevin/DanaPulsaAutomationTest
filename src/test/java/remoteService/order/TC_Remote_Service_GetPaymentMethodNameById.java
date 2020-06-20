package remoteService.order;

import java.text.ParseException;
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

public class TC_Remote_Service_GetPaymentMethodNameById extends TestBase {
	private String testCase;
	private String methodId;
	private String result;
	
	public TC_Remote_Service_GetPaymentMethodNameById(String testCase, String methodId, String result) {
		this.testCase = testCase;
		this.methodId = methodId;
		this.result = result;
	}

	@SuppressWarnings("unchecked")
	public void getPaymentMethodNameByIdRemoteService(String methodId) {
		logger.info("Call Get Payment Method Name By Id API [Order Domain]");
		logger.info("Test Data: ");
		logger.info("methodId id:" + methodId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("method", ConfigRemoteServiceOrder.QUEUE_GET_PAYMENT_METHOD_NAME_BY_ID);
		requestParams.put("message", methodId);
		
		RestAssured.baseURI = ConfigRemoteServiceOrder.BASE_URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.POST, ConfigRemoteServiceOrder.ENDPOINT_PATH);
		logger.info(response.getBody().asString());
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
	}
	
	@Test
	public void testGetPaymentMethodNameById() {
		getPaymentMethodNameByIdRemoteService(methodId);
		
		if (response.getStatusCode() != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testGetPaymentMethodNameById"})
	public void checkData() throws ParseException {
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains(result));
		
		final String errorMessage1 = "unknown method";
		final String errorMessage2 = "invalid request format";
		
		if (responseBody.contains(errorMessage1)) {
			// do some code
		} else if (responseBody.contains(errorMessage2)) {
			// do some code
		} else {
			Assert.assertEquals(response.getBody().asString(), "\"WALLET\"");		
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown method";
		final String errorMessage2 = "invalid request format";
		
		String responseBody = response.getBody().asString();
		switch (responseBody) {
		case errorMessage1:
			query = "SELECT * FROM payment_method WHERE id = ?";
			param.put("1", Long.parseLong(methodId));
			data = sqlExec(query, param, "order");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			// do some code
			break;
		default:
			query = "SELECT * FROM payment_method WHERE id = ?";
			param.put("1", Long.parseLong(methodId));
			data = sqlExec(query, param, "order");
			
			if (data.size() == 0) Assert.assertTrue(false, "no payment method found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(response.getBody().asString().substring(1,7), map.get("name"));
			}
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
