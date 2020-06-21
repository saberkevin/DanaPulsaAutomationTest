package remoteService.promotion;

import java.text.ParseException;
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
import io.restassured.path.json.JsonPath;
import model.User;
import remoteService.order.ConfigRemoteServiceOrder;

public class TC_Remote_Service_GetVoucherPromotion extends TestBase {
	private User user = new User();
	private String testCase;
	private String userId;
	private String page;
	private String result;
	private boolean isCreateUser;
	private String dataAMQP;
	private JsonPath responseData;
	
	public TC_Remote_Service_GetVoucherPromotion(String testCase, String userId, String page, String result) {
		this.testCase = testCase;
		this.userId = userId;
		this.page = page;
		this.result = result;
		isCreateUser = false;
	}
	
	@BeforeClass
	public void beforeClass() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Case:" + testCase);
		
		if (userId.equals("true")) {
			// initialize user
			user.setName(ConfigRemoteServiceOrder.USER_NAME);
			user.setEmail(ConfigRemoteServiceOrder.USER_EMAIL);
			user.setUsername(ConfigRemoteServiceOrder.USER_USERNAME);
			user.setPin(ConfigRemoteServiceOrder.USER_PIN);
			
			// insert user into database
			deleteBalanceByEmailByUsername(user.getEmail(), user.getUsername());
			deleteUserIfExist(user.getEmail(), user.getUsername());			
			createUser(user);
			user.setId(getUserIdByUsername(user.getUsername()));			
			createBalance(user.getId(), 10000000);
			userId = Long.toString(user.getId());
			
			// set flag
			isCreateUser = true;
		}
	}
	
	@Test
	public void testPromotionVouchers() {
		String message = "{\"userId\":" + userId + ",\"page\":" + page + "}";
		dataAMQP = callRP(promotionAMQP, ConfigRemoteServicePromotion.QUEUE_GET_VOUCHER_PROMOTION, message);
		responseData = new JsonPath(dataAMQP);
		logger.info("message = " + message);
		logger.info(dataAMQP);
	}
	
	@Test(dependsOnMethods = {"testPromotionVouchers"})
	public void checkData() throws ParseException {
		Assert.assertTrue(dataAMQP.contains(result), dataAMQP);
		
		final String errorMessage1 = "user not found";
		final String errorMessage2 = "[]";
		final String errorMessage3 = "invalid request format";
		
		if (dataAMQP.contains(errorMessage1)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage2)) {
			// do some code
		} else if (dataAMQP.contains(errorMessage3)) {
			// do some code
		} else {
			List<Map<String, String>> vouchers = responseData.get();
			Assert.assertTrue(vouchers.size() <= 10, "maximum vouchers per page is 10");
			
			for (int i = 0; i < vouchers.size(); i++) {
				Assert.assertNotNull(vouchers.get(i).get("id"));
				Assert.assertNotNull(vouchers.get(i).get("name"));
				Assert.assertNotNull(vouchers.get(i).get("voucherTypeName"));
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
		
		final String errorMessage1 = "user not found";
		final String errorMessage2 = "[]";
		final String errorMessage3 = "invalid request format";
		
		switch (dataAMQP) {
		case errorMessage1:
			query = "SELECT * FROM user WHERE id = ?";
			param.put("1", Long.parseLong(userId));
			data = sqlExec(query, param, "MEMBER");
			Assert.assertTrue(data.size() == 0);
			break;
		case errorMessage2:
			query = "SELECT A.id, A.name AS voucherName, B.name AS voucherTypeName, A.filePath, A.expiryDate "
					+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
					+ "WHERE A.isActive = 1 AND A.id NOT IN (SELECT voucherId FROM user_voucher where userId = ? AND voucherStatusId = 2) "
					+ "LIMIT ?, 10";
			param.put("1", Long.parseLong(userId));
			param.put("2", (Integer.parseInt(page)-1) * 10);
			data = sqlExec(query, param, "PROMOTION");
			Assert.assertTrue(data.size() == 0);
			break;			
		case errorMessage3:
			// do some code
			break;
		default:
			query = "SELECT A.id, A.name AS voucherName, B.name AS voucherTypeName, A.filePath, A.expiryDate "
					+ "FROM voucher A LEFT JOIN voucher_type B on A.typeId = B.id "
					+ "WHERE A.isActive = 1 AND A.id NOT IN (SELECT voucherId FROM user_voucher where userId = ? AND voucherStatusId = 2) "
					+ "ORDER BY A.id ASC LIMIT ?, 10";
			param.put("1", Long.parseLong(userId));
			param.put("2", (Integer.parseInt(page)-1) * 10);
			data = sqlExec(query, param, "PROMOTION");
			
			List<Map<String, Object>> vouchers = responseData.get();
			int index = 0;

			if (data.size() == 0) Assert.assertTrue(false, "no voucher found in database");
			for (Map<String, Object> map : data) {
				Assert.assertEquals(vouchers.get(index).get("id"), map.get("id"));
				Assert.assertEquals(vouchers.get(index).get("name"), map.get("voucherName"));						
				Assert.assertEquals(vouchers.get(index).get("voucherTypeName"), map.get("voucherTypeName"));						
				Assert.assertEquals(vouchers.get(index).get("filePath"), map.get("filePath"));	
				
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				Assert.assertEquals(formatter.format(vouchers.get(index).get("expiryDate")), formatter.format(map.get("expiryDate")));
				index++;
			}
			
			break;
		}
	}
	
	@AfterClass
	public void afterClass() {
		if (isCreateUser == true) {
			deleteBalanceByUserId(user.getId());
			deleteUserByEmailAndUsername(user.getEmail(), user.getUsername());
		}
		tearDown("Finished " + this.getClass().getSimpleName());
	}
}
