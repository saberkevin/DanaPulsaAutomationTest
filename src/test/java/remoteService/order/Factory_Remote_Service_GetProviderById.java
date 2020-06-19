package remoteService.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_GetProviderById extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String providerId, String result) {
		return new Object[] {new TC_Remote_Service_GetProviderById(testCase, providerId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigRemoteServiceOrder.SHEET_GET_PROVIDER_BY_ID, ConfigRemoteServiceOrder.SHEET_GET_PROVIDER_BY_ID);
	}
}
