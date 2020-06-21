package integrationtest.payment;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Integration_Payment extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String voucherId, String result) {
		return new Object[] {new TC_Integration_Payment(testCase, voucherId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigIntegrationTestPayment.FILE_PATH, ConfigIntegrationTestPayment.SHEET_PAYMENT);
	}
}
