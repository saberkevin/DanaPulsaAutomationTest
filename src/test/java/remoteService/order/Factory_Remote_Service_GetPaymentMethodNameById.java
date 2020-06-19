package remoteService.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_GetPaymentMethodNameById extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String methodId, String result) {
		return new Object[] {new TC_Remote_Service_GetPaymentMethodNameById(testCase, methodId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigRemoteServiceOrder.FILE_PATH, ConfigRemoteServiceOrder.SHEET_GET_PAYMENT_METHOD_NAME_BY_ID);
	}
}