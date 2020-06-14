package remoteService.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_GetProviderById extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String description, String providerId) {
		return new Object[] {new TC_Remote_Service_GetProviderById(description, providerId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/order/RemoteServiceOrderTestData.xlsx", "Get Provider By ID");
	}
}
