package remoteService.member;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Service_Verify_Pin_Login extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String id, String pin) {
		return new Object[] {new TC_Service_Verify_Pin_Login(id,pin)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(excelPrefix+"remoteService/member/pinServiceTestData.xlsx","VerifyPin");
	}
}
