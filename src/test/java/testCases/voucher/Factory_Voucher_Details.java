package testCases.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Voucher_Details extends TestBase {
	public Object[] createInstances(String sessionId, String voucherId) {
		return new Object[] {new TC_Voucher_Details(sessionId, voucherId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/login/loginTestData.xlsx");
	}
}
