package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_Unredeem  extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String userId, String voucherId, String result) {
		return new Object[] {new TC_Remote_Service_Unredeem(testCase, userId, voucherId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigRemoteServicePromotion.FILE_PATH, ConfigRemoteServicePromotion.SHEET_UNREDEEM);
	}
}
