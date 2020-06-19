package integrationtest.voucher;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Integration_VoucherPromotion  extends TestBase {
	@Factory(dataProvider="dp")
	public Object[] createInstances(String testCase, String page, String result) {
		return new Object[] {new TC_Integration_VoucherPromotion(testCase, page, result)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(ConfigIntegrationTestVoucher.FILE_PATH, ConfigIntegrationTestVoucher.SHEET_VOUCHER_PROMOTION);
	}
}
