package testCases.payment;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Pay_Order extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String sessionId, String transactionId, String paymentMethodId, String voucherId, String result) {
		return new Object[] {new TC_Pay_Order(testCase, sessionId, transactionId, paymentMethodId, voucherId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigApiTestPayment.FILE_PATH, ConfigApiTestPayment.SHEET_PAY_ORDER);
	}
}
