package remoteService.member;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Service_Login extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String phone) {
		return new Object[] {new TC_Service_Login(phone)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/remoteService/member/loginTestData.xlsx","Sheet1");
	}
}
