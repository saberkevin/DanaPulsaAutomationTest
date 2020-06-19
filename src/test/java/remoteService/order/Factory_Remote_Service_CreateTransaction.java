package remoteService.order;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_CreateTransaction extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String userId, String phoneNumber, String catalogId, String result) {
		return new Object[] {new TC_Remote_Service_CreateTransaction(testCase, userId, phoneNumber, catalogId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigRemoteServiceOrder.FILE_PATH, ConfigRemoteServiceOrder.SHEET_CREATE_TRANSACTION);
	}
}
