package remoteService.promotion;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Remote_Service_Unredeem  extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String description, String userId, String voucherId) {
		return new Object[] {new TC_Remote_Service_Unredeem(description, userId, voucherId)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/promotion/RemoteServicePromotionTestData.xlsx", "Unredeem");
	}
}
