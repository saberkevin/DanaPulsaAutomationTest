package remoteService.order;

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

public class TC_Remote_Service_GetPaymentMethodNameById extends TestBase {
	private String testCase;
	private String methodId;
	private String result;
	
	public TC_Remote_Service_GetPaymentMethodNameById() {
		
	}
	
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
		requestParams.put("method", "getPaymentMethodNameById");
		requestParams.put("message", methodId);
		
		RestAssured.baseURI = URIOrder;
		httpRequest = RestAssured.given();
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
				
		response = httpRequest.request(Method.POST, "/api/test/");
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
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		String responseBody = response.getBody().asString();

		if (responseBody.equals("unknown method")) {
			try {
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM payment_method WHERE id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(methodId));
				
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
				Connection conn = getConnectionOrder();
				String queryString = "SELECT * FROM payment_method WHERE id = ?";
				
				PreparedStatement ps = conn.prepareStatement(queryString);
				ps.setLong(1, Long.parseLong(methodId));
				
				ResultSet rs = ps.executeQuery();
				
				if (!rs.next()) {
					Assert.assertTrue(false, "no payment method found in database");
				}
				do {
					Assert.assertEquals(response.getBody().asString().substring(1,7), rs.getString("name"));
				} while(rs.next());
				
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
