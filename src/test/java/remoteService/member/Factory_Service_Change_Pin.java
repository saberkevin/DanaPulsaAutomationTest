package remoteService.member;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Service_Change_Pin extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String id, String pin) {
		return new Object[] {new TC_Service_Change_Pin(id,pin)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(excelPrefix+"remoteService/member/PinServiceTestData.xlsx","ChangePin");
	}
}
