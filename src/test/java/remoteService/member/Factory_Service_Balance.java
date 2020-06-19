package remoteService.member;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Service_Balance extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String id) {
		return new Object[] {new TC_Service_Balance(id)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData(excelPrefix+"remoteService/member/userServiceTestData.xlsx","Balance");
	}
}
