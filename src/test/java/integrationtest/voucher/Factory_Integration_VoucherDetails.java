package integrationtest.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Integration_VoucherDetails extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String voucherId, String result) {
		return new Object[] {new TC_Integration_VoucherDetails(testCase, voucherId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/integrationtest/voucher/VouchersIntegrationTestData.xlsx", "Voucher Details");
	}
}
