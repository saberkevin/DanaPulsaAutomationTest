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
	private String description;
	private String methodId;
	
	public TC_Remote_Service_GetPaymentMethodNameById() {
		
	}
	
	public TC_Remote_Service_GetPaymentMethodNameById(String description, String methodId) {
		this.description = description;
		this.methodId = methodId;
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
		logger.info("Case:" + description);
	}
	
	@Test
	public void testGetPaymentMethodNameById() {
		// call API get payment method name by id
		getPaymentMethodNameByIdRemoteService(methodId);
		
		int statusCode = response.getStatusCode();

		if (statusCode != 200) {
			logger.info(response.getBody().asString());
			Assert.assertTrue(false, "cannot hit API");
		}
	}
	
	@Test(dependsOnMethods = {"testGetPaymentMethodNameById"})
	public void checkData() throws ParseException {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			String responseBody = response.getBody().asString();
			
			if (!responseBody.equals("unknown method") && !responseBody.equals("invalid request format")) {
				Assert.assertEquals(responseBody, "\"WALLET\"");
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
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
						Assert.assertTrue(false, "no payment method data");
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
	}
	
	@AfterClass
	public void afterClass() {
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
