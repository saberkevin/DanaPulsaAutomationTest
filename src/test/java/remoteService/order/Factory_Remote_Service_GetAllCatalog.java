package remoteService.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_GetAllCatalog extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String phonePrefix, String result) {
		return new Object[] {new TC_Remote_Service_GetAllCatalog(testCase, phonePrefix, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/order/RemoteServiceOrderTestData.xlsx", "Get All Catalog");
	}
}
