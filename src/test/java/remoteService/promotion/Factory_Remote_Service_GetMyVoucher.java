package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_GetMyVoucher extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String userId, String page, String result) {
		return new Object[] {new TC_Remote_Service_GetMyVoucher(testCase, userId, page, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigRemoteServicePromotion.FILE_PATH, ConfigRemoteServicePromotion.SHEET_GET_MY_VOUCHER);
	}
}
