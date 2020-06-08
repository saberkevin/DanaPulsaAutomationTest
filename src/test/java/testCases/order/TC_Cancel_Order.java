package testCases.order;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.Transaction;
import model.User;

public class TC_Cancel_Order extends TestBase {
	private User user;
	private Transaction transaction;
	
	public TC_Cancel_Order(String transactionId) {
		transaction.setId(transactionId);
	}
	
	@BeforeClass
	public void beforeClass() {
		
	}
	
	@BeforeMethod
	public void berforeMethod() {
		
	}
	
	@Test
	public void testCancelOrder() {
		
	}

	@AfterMethod
	public void afterMethod() {
		
	}
	
	@AfterClass
	public void afterClass() {
		
	}
}
