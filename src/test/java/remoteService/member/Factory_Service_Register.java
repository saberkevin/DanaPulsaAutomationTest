package remoteService.member;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Service_Register extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String name, String email, String phone, String pin) {
		return new Object[] {new TC_Service_Register(name,email,phone,pin)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(excelPrefix+"remoteService/member/registerServiceTestData.xlsx","Sheet1");
	}
}
