package testCases.otp;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Get_Otp extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String id) {
		return new Object[] {new TC_Get_Otp(id)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/otp/getOtpTestData.xlsx");
	}
}
