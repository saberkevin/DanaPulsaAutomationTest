package testCases.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Promotion_Vouchers extends TestBase {
	public Object[] createInstances(String sessionId, String page) {
		return new Object[] {new TC_Promotion_Vouchers(sessionId, page)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/login/loginTestData.xlsx");
	}
}
