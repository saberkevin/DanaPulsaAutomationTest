package testCases.order;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.Catalog;
import model.User;

public class TC_Create_Order extends TestBase {
	private User user;
	private String phoneNumber;
	private Catalog catalog;
	
	public TC_Create_Order(String phoneNumber, String catalogId) {
		this.phoneNumber = phoneNumber;
		catalog.setId(catalogId);
	}
	
	@BeforeClass
	public void beforeClass() {
		
	}
	
	@BeforeMethod
	public void berforeMethod() {
		
	}
	
	@Test
	public void testCreateOrder() {
		
	}

	@AfterMethod
	public void afterMethod() {
		
	}
	
	@AfterClass
	public void afterClass() {
		
	}
}
