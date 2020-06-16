package testCases.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Voucher_Details extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String voucherId, String result) {
		return new Object[] {new TC_Voucher_Details(testCase, sessionId, voucherId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/voucher/VouchersTestData.xlsx", "Voucher Details");
	}
}
