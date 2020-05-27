package base;

import org.apache.log4j.Logger;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utilities.ExcelUtil;

public class TestBase {
	public RequestSpecification httpRequest;
	public Response response;
	
	public String URI = "https://be-emoney.herokuapp.com/api";
	public String tokenBypass = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkd2lnaHRAZHVuZGVybWlmZmxpbi5jbyIsImV4cCI6MTYyMDk4ODA0NiwiaWF0IjoxNTg5NDUyMDQ2fQ.brk2Tk9Yv8SwKSAH_UusX06ZL3AtonGlWbB7uT0i6GsDRmV_1DCaHv2LOjfuU8xNf5Y8t8Um-WDTNiXwhu4qAg";
	
	public Logger logger;
	
	@BeforeClass
	public void setup()
	{
		logger = Logger.getLogger("restAPI");;
		PropertyConfigurator.configure("../SigmaCardAutomationTest/src/Log4j.properties");
		logger.setLevel(Level.DEBUG);
	}
	
	public String[][] getExcelData(String filePath) throws IOException
	{
		String path = filePath;
		
		int rowCount = ExcelUtil.getRowCount(path, "Sheet1");
		int colCount = ExcelUtil.getCellCount(path, "Sheet1",1);
		
		String data[][] = new String[rowCount][colCount];
		
		for(int i=1; i<=rowCount; i++)
		{
			for(int j=0; j<colCount; j++)
			{
				data[i-1][j] = ExcelUtil.getCellData(path, "Sheet1", i, j);
			}
		}
		
		return data;
	}
	
	public void checkResponseBodyNotEmpty()
	{
		logger.info("***** Check Response Body (Not Empty) *****");
		
		String responseBody = response.getBody().asString();
		logger.info("Response Body = " + responseBody);
		Assert.assertTrue(responseBody != null && !responseBody.contains("No message available"));
	}
	
	public void checkStatusCode(String sc)
	{
		logger.info("***** Check Status Code *****");
		
		int statusCode = response.getStatusCode();
		logger.info("Status Code = " + statusCode);
		Assert.assertEquals(statusCode, Integer.parseInt(sc));	
	}
	
	public void checkResponseTime(String rt)
	{
		logger.info("***** Check Response Time *****");
		
		long responseTime = response.getTime();
		logger.info("Response Time = " + responseTime);
		Assert.assertTrue(responseTime<Long.parseLong(rt));
	}
	
	public void tearDown(String message)
	{
		logger.info("***** " + message + " *****");	
		httpRequest = null;
		response = null;
	}
}
