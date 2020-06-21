package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_Issue extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String userId, String price, String providerId, String voucherId, String paymentMethodId, String result) {
		return new Object[] {new TC_Remote_Service_Issue(testCase, userId, price, providerId, voucherId, paymentMethodId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigRemoteServicePromotion.FILE_PATH, ConfigRemoteServicePromotion.SHEET_ISSUE_VOUCHER);
	}
}
