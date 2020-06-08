package testCases.history;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_History_In_Progress extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String id) {
		return new Object[] {new TC_History_In_Progress(id)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/history/historyInProgressTestData.xlsx");
	}
}
