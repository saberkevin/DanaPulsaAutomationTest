package testCases.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Create_Order extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String phoneNumber, String catalogId, String result) {
		return new Object[] {new TC_Create_Order(testCase, sessionId, phoneNumber, catalogId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigApiTestOrder.FILE_PATH, ConfigApiTestOrder.SHEET_CREATE_ORDER);
	}
}
