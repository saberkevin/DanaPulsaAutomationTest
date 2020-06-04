package testCases.payment;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import base.TestBase;
import model.Transaction;
import model.User;
import model.Voucher;

public class TC_Pay_Order extends TestBase {
	private User user;
	private String phoneNumber;
	private Transaction transaction;
	private Voucher voucher;
	
	@BeforeClass
	public void beforeClass() {
		
	}
	
	@BeforeMethod
	public void berforeMethod() {
		
	}
	
	@Test
	public void testRecentPhoneNumber() {
		
	}

	@AfterMethod
	public void afterMethod() {
		
	}
	
	@AfterClass
	public void afterClass() {
		
	}
}
