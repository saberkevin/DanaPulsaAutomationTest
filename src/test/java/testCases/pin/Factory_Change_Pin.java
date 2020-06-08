package testCases.pin;

import java.io.IOException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import base.TestBase;

public class Factory_Change_Pin extends TestBase{
	@Factory(dataProvider="dp")
	public Object[] createInstances(String pin) {
		return new Object[] {new TC_Change_Pin(pin)};
	}
	
	@DataProvider(name="dp")
	public String[][] dataProvider() throws IOException {
		return getExcelData("../DanaPulsaAutomationTest/src/test/java/testCases/pin/changePinTestData.xlsx");
	}
}
