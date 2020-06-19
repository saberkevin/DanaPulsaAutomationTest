package testCases.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_My_Vouchers extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String page, String result) {
		return new Object[] {new TC_My_Vouchers(testCase, sessionId, page, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/voucher/VouchersTestData.xlsx", "My Vouchers");
	}
}
