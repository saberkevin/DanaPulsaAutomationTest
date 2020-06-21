package remoteService.order;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;

public class TC_Remote_Service_GetPaymentMethodNameById extends TestBase {
	private String testCase;
	private String methodId;
	private String result;
	private String dataAMQP;
	
	public TC_Remote_Service_GetPaymentMethodNameById(String testCase, String methodId, String result) {
		this.testCase = testCase;
		this.methodId = methodId;
		this.result = result;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
	}
	
	@Test
	public void testGetPaymentMethodNameById() {
		dataAMQP = callRP(orderAMQP, ConfigRemoteServiceOrder.QUEUE_GET_PAYMENT_METHOD_NAME_BY_ID, methodId);
		logger.info("message = " + methodId);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testGetPaymentMethodNameById"})
	public void checkData() throws ParseException {
		Assert.assertTrue(dataAMQP.contains(result));
		
		final String errorMessage1 = "unknown method";
		final String errorMessage2 = "invalid request format";
		
		if (dataAMQP.contains(errorMessage1)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage2)) {
			// do some code
		} else {
			Assert.assertEquals(dataAMQP, "\"WALLET\"");		
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		final String errorMessage1 = "unknown method";
		final String errorMessage2 = "invalid request format";
		
		switch (dataAMQP) {
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
				Assert.assertEquals(dataAMQP.substring(1,7), map.get("name"));
			}
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
