package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_GetVoucherPromotion extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String description, String userId, String page, String result) {
		return new Object[] {new TC_Remote_Service_GetVoucherPromotion(description, userId, page, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/promotion/RemoteServicePromotionTestData.xlsx", "Get Promotion Vouchers");
	}
}
