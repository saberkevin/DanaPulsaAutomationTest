package testCases.voucher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.TestBase;
import model.User;
import remoteService.order.ConfigRemoteServiceOrder;

public class TC_My_Vouchers extends TestBase {
	private User user = new User();
	private String testCase;
	private String sessionId;
	private String page;
	private String result;
	private boolean isCreateUser;
	
	public TC_My_Vouchers(String testCase, String sessionId, String page, String result) {
		this.testCase = testCase;
		this.sessionId = sessionId;
		this.page = page;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (sessionId.equals("true")) {			
			// insert user into database
			user.setName(ConfigRemoteServiceOrder.USER_NAME);
			user.setEmail(ConfigRemoteServiceOrder.USER_EMAIL);
			user.setUsername(ConfigRemoteServiceOrder.USER_USERNAME);
			user.setPin(ConfigRemoteServiceOrder.USER_PIN);
			
			// delete if exist
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));
			createBalance(user.getId(), 10000000);
			
			verifyPinLogin(Long.toString(user.getId()), Integer.toString(user.getPin()));
			checkStatusCode("200");
			user.setSessionId(response.getCookie("JSESSIONID"));
			sessionId = user.getSessionId();

			// insert voucher into database
			if (testCase.equals("Valid user id and page (below 10 vouchers)")) {					
				createUserVoucher(user.getId(), 1, 2);			
			} else if (testCase.equals("Valid user id and page (more than 10 vouchers)")) {
				for (int i = 0; i < 11; i++) {		
					createUserVoucher(user.getId(), i + 1, 2);			
				}
			}
			
			// set flag
			isCreateUser = true;
		}
	}

	@Test
	public void testMyVouchers() {
		getMyVoucher(sessionId, page);
		user.setSessionId(response.getCookie("JSESSIONID"));
		
		Assert.assertTrue(response.getBody().asString().contains(result));

		int statusCode = response.getStatusCode();		
		if (statusCode == 401) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "401");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "Unauthorized");
		} else if (statusCode == 404) {
			Assert.assertTrue(response.getBody().asString().contains("Not Found") 
					|| response.getBody().asString().contains("you don’t have any vouchers"));
		} else if (statusCode == 200) {
			Assert.assertEquals(response.getBody().jsonPath().getString("code"), "200");
			Assert.assertEquals(response.getBody().jsonPath().getString("message"), "success");
		}
	}
	
	@Test(dependsOnMethods = {"testMyVouchers"})
	public void checkData() {
		int statusCode = response.getStatusCode();
		
		if (statusCode == 200) {
			List<Map<String, String>> vouchers = response.getBody().jsonPath().getList("data");			
			Assert.assertTrue(vouchers.size() <= 10, "maximum vouchers per page is 10");
			
			for (int i = 0; i < vouchers.size(); i++) {
				Assert.assertNotNull(vouchers.get(i).get("id"));
				Assert.assertNotNull(vouchers.get(i).get("name"));
				Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
				Assert.assertNotNull(vouchers.get(i).get("discount"));
				Assert.assertNotNull(vouchers.get(i).get("maxDeduction"));
				Assert.assertNotNull(vouchers.get(i).get("filePath"));
				Assert.assertNotNull(vouchers.get(i).get("expiryDate"));
			}
		}
	}
	
	@Test(dependsOnMethods = {"checkData"})
	public void checkDB() {
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String query = "";
		
		int statusCode = response.getStatusCode();		
		if (statusCode == 200) {
			query = "SELECT A.voucherId, B.name AS voucherName, B.discount, C.name AS voucherTypeName, B.maxDeduction, B.filePath, B.expiryDate "
					+ "FROM user_voucher A LEFT JOIN voucher B on A.voucherId = B.id "
					+ "LEFT JOIN voucher_type C on B.typeId = C.id "
					+ "WHERE A.voucherStatusId != 1 AND B.isActive = 1 AND A.userId = ? "
					+ "ORDER BY A.voucherId ASC LIMIT ?, 10";
			param.put("1", user.getId());
			param.put("2", (Integer.parseInt(page)-1) * 10);
			data = sqlExec(query, param, "PROMOTION");
			
			List<Map<String, Object>> vouchers = response.getBody().jsonPath().getList("data");
			int index = 0;

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(vouchers.get(index).get("id"), map.get("voucherId"));
				Assert.assertEquals(vouchers.get(index).get("name"), map.get("voucherName"));					
				Assert.assertEquals(vouchers.get(index).get("discount"), map.get("discount"));					
				Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), map.get("voucherTypeName"));						
				Assert.assertEquals(Long.valueOf((Integer) vouchers.get(index).get("maxDeduction")), map.get("maxDeduction"));					
				Assert.assertEquals(vouchers.get(index).get("filePath"), map.get("filePath"));	
				
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				Assert.assertEquals(formatter.format(vouchers.get(index).get("expiryDate")), formatter.format(map.get("expiryDate")));
				index++;
			}
		} else if (statusCode == 404) {
			if (response.getBody().asString().contains("you don’t have any vouchers")) {
				if (response.getBody().asString().contains("you don’t have any vouchers")) {
					query = "SELECT A.voucherId, B.name AS voucherName, B.discount, C.name AS voucherTypeName, B.maxDeduction, B.filePath, B.expiryDate "
							+ "FROM user_voucher A LEFT JOIN voucher B on A.voucherId = B.id "
							+ "LEFT JOIN voucher_type C on B.typeId = C.id "
							+ "WHERE A.voucherStatusId != 1 AND B.isActive = 1 AND A.userId = ? "
							+ "ORDER BY A.voucherId ASC LIMIT ?, 10";
					param.put("1", user.getId());
					param.put("2", (Integer.parseInt(page)-1) * 10);
					data = sqlExec(query, param, "PROMOTION");
					Assert.assertTrue(data.size() == 0);
				}
			}
		}
	}
	
	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteUserVoucherByUserId(user.getId());
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
