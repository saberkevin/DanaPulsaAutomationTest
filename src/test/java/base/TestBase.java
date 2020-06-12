package base;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.User;
import utilities.ExcelUtil;

@SuppressWarnings("unchecked")
public class TestBase {
	private static final String REGISTER_PATH = "/api/register";
	private static final String LOGIN_PATH = "/api/login";
	private static final String VERIFY_PIN_LOGIN_PATH = "/api/verifypinlogin";
	private static final String FORGOT_PIN_OTP_PATH = "/api/forgotpin-otp";
	private static final String CHANGE_PIN_OTP_PATH = "/api/changepin-otp";
	private static final String VERIFY_OTP_PATH = "/api/verify-otp";
	private static final String GET_OTP_PATH = "/api/otp/";
	private static final String CHANGE_PIN_PATH = "/api/changepin";
	private static final String GET_PROFILE_PATH = "/api/profile";
	private static final String GET_BALANCE_PATH = "/api/balance";
	private static final String LOGOUT_PATH = "/api/logout";
	private static final String RECENT_PHONE_NUMBER_PATH = "/api/recentnumber";
	private static final String CATALOG_PATH = "/api/catalog/";
	private static final String ORDER_PATH = "/api/order";
	private static final String CANCEL_ORDER_PATH = "/api/transaction/cancel/";
	private static final String PAYMENT_PATH = "/api/pay";
	private static final String MY_VOUCHER_PATH = "/api/my-vouchers/";
	private static final String PROMOTION_VOUCHER_PATH = "/api/vouchers/promotion/";
	private static final String RECOMMENDATION_VOUCHER_PATH = "/api/vouchers/recommendation/";
	private static final String VOUCHER_DETAILS_PATH = "/api/vouchers/details/";
	private static final String HISTORY_IN_PROGRESS_PATH = "/api/transaction/in-progress/";
	private static final String HISTORY_COMPLETED_PATH = "/api/transaction/completed/";
	private static final String HISTORY_DETAILS_PATH = "/api/transaction/details/";
	
	public RequestSpecification httpRequest;
	public Response response;
	public String URI = "http://ef23238f08ec.ngrok.io";	
	public Logger logger;
	
	@BeforeClass
	public void setup()
	{
		logger = Logger.getLogger("restAPI");;
		PropertyConfigurator.configure("../DanaPulsaAutomationTest/src/Log4j.properties");
		logger.setLevel(Level.DEBUG);
	}
	
	public String[][] getExcelData(String filePath, String sheetName) throws IOException
	{
		String path = filePath;
		
		int rowCount = ExcelUtil.getRowCount(path, sheetName);
		int colCount = ExcelUtil.getCellCount(path, sheetName,1);
		
		String data[][] = new String[rowCount][colCount];
		
		for(int i=1; i<=rowCount; i++)
		{
			for(int j=0; j<colCount; j++)
			{
				data[i-1][j] = ExcelUtil.getCellData(path, sheetName, i, j);
			}
		}
		return data;
	}
	
	public void checkStatusCode(String sc)
	{
		logger.info("***** Check Status Code *****");
		
		int statusCode = response.getStatusCode();
		logger.info("Status Code = " + statusCode);
		Assert.assertEquals(statusCode, Integer.parseInt(sc));	
	}
	
	public void checkResponseTime(String rt)
	{
		logger.info("***** Check Response Time *****");
		
		long responseTime = response.getTime();
		logger.info("Response Time = " + responseTime);
		Assert.assertTrue(responseTime<Long.parseLong(rt));
	}
	
	public void checkEmailValid(String email)
	{
		String regex = "^(.+)@(.+).(.+)$";
		 
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		
		boolean isValid = matcher.matches();
		
		Assert.assertTrue(isValid);	
	}
	
	public void checkResultPhoneValid(String phone)
	{
		String regex = "^628[0-9]{9,13}$";
		 
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(phone);
		
		boolean isValid = matcher.matches();
		
		Assert.assertTrue(isValid);	
	}
	
	public void checkPinValid(String pin)
	{
		String regex = "^[1-9][0-9]{5}$";
		 
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(pin);
		
		boolean isValid = matcher.matches();
		
		Assert.assertTrue(isValid);	
	}
	
	public String replacePhoneForAssertion(String phone)
	{
		String phoneSubstring = phone;
		if(phone.startsWith("0") || phone.startsWith("62") || phone.startsWith("+62"))
		{
			phoneSubstring = phone.substring(phone.indexOf("8"));
		}
		return "62"+phoneSubstring;
	}
	
	public void tearDown(String message)
	{
		logger.info("***** " + message + " *****");	
		httpRequest = null;
		response = null;
	}
	
	//============ Request ==============================//
	
	public void register(String name, String email, String phone, String pin) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("name:" + name);
		logger.info("email:" + email);
		logger.info("phone:" + phone);
		logger.info("pin:" + pin);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("name", name);
		requestParams.put("email", email);
		requestParams.put("phone", phone);
		requestParams.put("pin", Integer.parseInt(pin));
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, REGISTER_PATH);
	}
	
	public void login(String phone) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("phone:" + phone);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("phone", phone);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, LOGIN_PATH);
	}
	
	public void verifyPinLogin(String id, String pin) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("pin:" + pin);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("id", Long.parseLong(id));
		requestParams.put("pin", Integer.parseInt(pin));
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, VERIFY_PIN_LOGIN_PATH);
	}
	
	public void getProfile() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, GET_PROFILE_PATH);
	}
	
	public void getBalance() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, GET_BALANCE_PATH);
	}
	
	public void changePinOtp() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.POST, CHANGE_PIN_OTP_PATH);
	}
	
	public void forgotPinOtp(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("id", Long.parseLong(id));
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, FORGOT_PIN_OTP_PATH);
	}
	
	public void verifyOtp(String id, String code) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("code:" + code);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("id", Long.parseLong(id));
		requestParams.put("code", code);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, VERIFY_OTP_PATH);
	}
	
	public void changePin(String pin) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("pin:" + pin);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("pin", Integer.parseInt(pin));
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, CHANGE_PIN_PATH);
	}
	
	public void GetOtp(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, GET_OTP_PATH+id);
	}
	
	public void logout() {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.DELETE, LOGOUT_PATH);
	}
	
	public void historyInProgress(String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, HISTORY_IN_PROGRESS_PATH+page);
	}
	
	public void historyCompleted(String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, HISTORY_COMPLETED_PATH+page);
	}
	
	public void historyDetail(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, HISTORY_DETAILS_PATH+id);
	}
	
	public void getRecentPhoneNumber(String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);

		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "SESSION=" + sessionId);

		response = httpRequest.request(Method.GET, RECENT_PHONE_NUMBER_PATH);
	}
	
	public void getCatalog(String sessionId, String phoneNumber) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("phone number:" + phoneNumber);

		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "SESSION=" + sessionId);
		
		response = httpRequest.request(Method.GET, CATALOG_PATH + phoneNumber);
	}
	
	public void createOrder(String sessionId, String phoneNumber, long catalogId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("phone number:" + phoneNumber);
		logger.info("catalog id:" + catalogId);
				
		JSONObject requestParams = new JSONObject();
		requestParams.put("phone", phoneNumber);
		requestParams.put("catalogId", catalogId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "SESSION=" + sessionId);
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, ORDER_PATH);
	}
	
	public void cancelOrder(String sessionId, long transactionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("transaction id:" + transactionId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "SESSION=" + sessionId);
		
		response = httpRequest.request(Method.DELETE, CANCEL_ORDER_PATH + transactionId);
	}
	
	public void payOrder(String sessionId, long transactionId, long paymentMethodId, long voucherId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("transaction id:" + transactionId);
		logger.info("payment method id:" + paymentMethodId);
		logger.info("voucher id:" + voucherId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("transactionId", transactionId);
		requestParams.put("methodId", paymentMethodId);
		requestParams.put("voucherId", voucherId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "SESSION=" + sessionId);
		httpRequest.header("Content-Type", "application/json");
		
		response = httpRequest.request(Method.POST, PAYMENT_PATH);
	}
	
	public void getMyVoucher(String sessionId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "SESSION=" + sessionId);
		
		response = httpRequest.request(Method.GET, MY_VOUCHER_PATH + page);
	}
	
	public void getPromotionVoucher(String sessionId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Cookie", "SESSION=" + sessionId);

		response = httpRequest.request(Method.GET, PROMOTION_VOUCHER_PATH + page);
	}
	
	public void getRecommendationVoucher(String sessionId, long transactionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("transaction id:" + transactionId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Cookie", "SESSION=" + sessionId);
		
		response = httpRequest.request(Method.GET, RECOMMENDATION_VOUCHER_PATH + transactionId);
	}
	
	public void getVoucherDetails(String sessionId, long voucherId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("voucher id:" + voucherId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Cookie", "SESSION=" + sessionId);

		response = httpRequest.request(Method.GET, VOUCHER_DETAILS_PATH + Long.toString(voucherId));
	}
	
	//============ DB Connection ==============================//
	
	public Connection getConnectionOrder() {
		Connection conn = null;
		String dbUrl = "jdbc:mysql://remotemysql.com:3306/Cwyx6vUQDe";					
		String username = "Cwyx6vUQDe";	
		String password = "J8hC6uAYxS";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, username, password);
			conn.setAutoCommit(true);			
		} catch (Exception e) {
			e.printStackTrace();			
		}

		return conn;
	}
	
	public Connection getConnectionPromotion() {
		Connection conn = null;
		String dbUrl = "jdbc:mysql://remotemysql.com:3306/2XXZFHdio8";					
		String username = "2XXZFHdio8";	
		String password = "CiJU7VdZB5";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, username, password);
			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();			
		}

		return conn;
	}
	
	public Connection getConnectionMember() {
		Connection conn = null;
		String dbUrl = "jdbc:mysql://remotemysql.com:3306/fNmIfiTyXD";					
		String username = "fNmIfiTyXD";	
		String password = "VcTDEMaZ6V";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, username, password);
			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();			
		}

		return conn;
	}
	
	//============ Some Command to DB ==============================//
	
	public void createUser(User user) {
		try {
			Connection conn = getConnectionMember();
			String query = "INSERT INTO user(name, email, username, pin) VALUES(?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, user.getName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getUsername());
			ps.setLong(4, user.getPin());
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public void deleteUserById(long id) {
		try {
			Connection conn = getConnectionMember();
			String query = "DELETE FROM user WHERE id = ?";

			PreparedStatement ps = conn.prepareStatement(query);			
			ps.setLong(1, id);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteUserIfExist(String email, String username) {
		try {
			Connection conn = getConnectionMember();
			String query = "DELETE FROM user WHERE EXISTS (SELECT id FROM user WHERE email = ? OR username = ?)";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, email);
			ps.setString(2,username);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public long getUserIdByUsername(String username) {
		long id = 0;
		
		try {
			Connection conn = getConnectionMember();
			String query = "SELECT id FROM user WHERE username = ?";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, username);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				id = rs.getLong("id");
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return id;
	}
	
	public void createVoucherForUser(long userId, long voucherId, long voucherStatusId) {
		try {
			Connection conn = getConnectionPromotion();
			String query = "INSERT INTO user_voucher(userId, voucherId, voucherStatusId, createdAt) VALUES(?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setLong(1, userId);
			ps.setLong(2, voucherId);
			ps.setLong(3, voucherStatusId);
			ps.setDate(4, (java.sql.Date) new Date());
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
}
