package remoteService.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_Cancel extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String userId, String transactionId, String result) {
		return new Object[] {new TC_Remote_Service_Cancel(testCase, userId, transactionId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/order/RemoteServiceOrderTestData.xlsx", "Cancel");
	}
}
