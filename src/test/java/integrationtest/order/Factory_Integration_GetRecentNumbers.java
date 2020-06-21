package integrationtest.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Integration_GetRecentNumbers extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String result) {
		return new Object[] {new TC_Integration_GetRecentNumbers(testCase, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigIntegrationTestOrder.FILE_PATH, ConfigIntegrationTestOrder.SHEET_RECENT_NUMBER);
	}
}
