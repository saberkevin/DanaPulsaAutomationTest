package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_GetVoucherPromotion extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String userId, String page) {
		return new Object[] {new TC_Remote_Service_GetVoucherPromotion(userId, page)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/promotion/RemoteServicePromotionTestData.xlsx", "Get Promotion Vouchers");
	}
}
