package testCases.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Recent_Phone_Number extends TestBase {	
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String result) {
		return new Object[] {new TC_Recent_Phone_Number(testCase, sessionId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/order/OrderTestData.xlsx", "Recent Phone Number");
	}
}
