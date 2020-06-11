package testCases.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Mobile_Recharge_Catalog extends TestBase {
	public Object[] createInstances(String sessionId, String phoneNumber) {
		return new Object[] {new TC_Mobile_Recharge_Catalog(sessionId, phoneNumber)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/order/OrderTestData.xlsx", "Catalog");
	}
}
