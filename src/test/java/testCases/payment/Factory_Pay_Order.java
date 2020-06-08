package testCases.payment;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Pay_Order extends TestBase {
	public Object[] createInstances(String sessionId, String transactionId, String paymentMethodId, String voucherId) {
		return new Object[] {new TC_Pay_Order(sessionId, transactionId, paymentMethodId, voucherId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/login/loginTestData.xlsx");
	}
}
