package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_Redeem extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String userId, String voucherId, String price, String paymentMethodId, String providerId, String result) {
		return new Object[] {new TC_Remote_Service_Redeem(testCase, userId, voucherId, price, paymentMethodId, providerId, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigRemoteServicePromotion.FILE_PATH, ConfigRemoteServicePromotion.SHEET_REDEEM);
	}
}
