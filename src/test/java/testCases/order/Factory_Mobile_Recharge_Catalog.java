package testCases.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Mobile_Recharge_Catalog extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String phonePrefix, String result) {
		return new Object[] {new TC_Mobile_Recharge_Catalog(testCase, sessionId, phonePrefix, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/order/OrderTestData.xlsx", "Catalog");
	}
}
