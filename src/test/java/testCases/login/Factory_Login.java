package testCases.login;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Login extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String phone) {
		return new Object[] {new TC_Login(phone)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(excelPrefix+"testCases/login/loginTestData.xlsx","Sheet1");
	}
}
