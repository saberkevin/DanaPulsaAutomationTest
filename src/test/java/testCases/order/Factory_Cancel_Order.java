package testCases.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Cancel_Order extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String transactionId, String result) {
		return new Object[] {new TC_Cancel_Order(testCase, sessionId, transactionId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigApiTestOrder.FILE_PATH, ConfigApiTestOrder.SHEET_CANCEL_ORDER);
	}
}
