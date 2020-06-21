package integrationtest.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Integration_GetAllCatalog extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String phonePrefix, String result) {
		return new Object[] {new TC_Integration_GetAllCatalog(testCase, phonePrefix, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigIntegrationTestOrder.FILE_PATH, ConfigIntegrationTestOrder.SHEET_CATALOG);
	}
}
