package testCases.otp;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Verify_Otp extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String id, String code) {
		return new Object[] {new TC_Verify_Otp(id, code)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(excelPrefix+"testCases/otp/otpTestData.xlsx","VerifyOtp");
	}
}
