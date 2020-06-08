package testCases.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Recommendation_Vouchers extends TestBase {
	public Object[] createInstances(String sessionId, String transactionId) {
		return new Object[] {new TC_Recommendation_Vouchers(sessionId, transactionId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/login/loginTestData.xlsx");
	}
}
