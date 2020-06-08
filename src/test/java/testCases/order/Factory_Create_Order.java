package testCases.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Create_Order extends TestBase {
	public Object[] createInstances(String sessionId, String phoneNumber, String catalogId) {
		return new Object[] {new TC_Create_Order(sessionId, phoneNumber, catalogId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/login/loginTestData.xlsx");
	}
}
