package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import base.TestBase;

public class Factory_Remote_Service_GetVoucherDetail extends TestBase {
	public Object[] createInstances(String voucherId) {
		return new Object[] {new TC_Remote_Service_GetVoucherDetail(voucherId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/promotion/RemoteServicePromotionTestData.xlsx", "Get Voucher Details");
	}
}
