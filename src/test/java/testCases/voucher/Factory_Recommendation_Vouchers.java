package testCases.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Recommendation_Vouchers extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String transactionId, String result) {
		return new Object[] {new TC_Recommendation_Vouchers(testCase, sessionId, transactionId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/voucher/VouchersTestData.xlsx", "Recommendation Vouchers");
	}
}
