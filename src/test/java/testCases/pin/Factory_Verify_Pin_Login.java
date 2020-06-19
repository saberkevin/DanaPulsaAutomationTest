package testCases.pin;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Verify_Pin_Login extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String id, String pin) {
		return new Object[] {new TC_Verify_Pin_Login(id,pin)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(excelPrefix+"testCases/pin/pinTestData.xlsx","VerifyPin");
	}
}
