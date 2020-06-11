package testCases.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Cancel_Order extends TestBase {
	public Object[] createInstances(String sessionId, String transactionId) {
		return new Object[] {new TC_Cancel_Order(sessionId, transactionId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/order/OrderTestData.xlsx", "Cancel Order");
	}
}
