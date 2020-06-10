package base;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

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
import utilities.ExcelUtil;

@SuppressWarnings("unchecked")
public class TestBase {
	private static final String REGISTER_PATH = "/api/member/register";
	private static final String LOGIN_PATH = "/api/member/login";
	private static final String VERIFY_PIN_LOGIN_PATH = "/api/member/verifypinlogin";
	private static final String FORGOT_PIN_OTP_PATH = "/api/member/forgotpin-otp";
	private static final String CHANGE_PIN_OTP_PATH = "/api/member/changepin-otp";
	private static final String VERIFY_OTP_PATH = "/api/member/verify-otp";
	private static final String GET_OTP_PATH = "/api/member/get-otp/";
	private static final String CHANGE_PIN_PATH = "/api/member/changepin";
	private static final String GET_PROFILE_PATH = "/api/member/getprofile";
	private static final String GET_BALANCE_PATH = "/api/member/getbalance";
	private static final String LOGOUT_PATH = "/api/logout";
	private static final String RECENT_PHONE_NUMBER_PATH = "/api/recentnumber";
	private static final String CATALOG_PATH = "/api/catalog";
	private static final String ORDER_PATH = "/api/order";
	private static final String CANCEL_ORDER_PATH = "/api/transaction/cancel/";
	private static final String PAYMENT_PATH = "/api/pay";
	private static final String MY_VOUCHER_PATH = "/api/my-vouchers/";
	private static final String PROMOTION_VOUCHER_PATH = "/api/voucher/promotion/";
	private static final String RECOMMENDATION_VOUCHER_PATH = "/api/vouchers/recommendation";
	private static final String VOUCHER_DETAILS_PATH = "/api/voucher/";
	private static final String HISTORY_IN_PROGRESS_PATH = "/api/transaction/in-progress/";
	private static final String HISTORY_COMPLETED_PATH = "/api/transaction/completed/";
	private static final String HISTORY_DETAILS_PATH = "/api/transaction/details/";
	
	public RequestSpecification httpRequest;
	public Response response;
	
	public String URI = "https://be-emoney.herokuapp.com/api";
	public String tokenBypass = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkd2lnaHRAZHVuZGVybWlmZmxpbi5jbyIsImV4cCI6MTYyMDk4ODA0NiwiaWF0IjoxNTg5NDUyMDQ2fQ.brk2Tk9Yv8SwKSAH_UusX06ZL3AtonGlWbB7uT0i6GsDRmV_1DCaHv2LOjfuU8xNf5Y8t8Um-WDTNiXwhu4qAg";
	
	public Logger logger;
	public Connection con;
	
	@BeforeClass
	public void setup()
	{
		logger = Logger.getLogger("restAPI");;
		PropertyConfigurator.configure("../DanaPulsaAutomationTest/src/Log4j.properties");
		logger.setLevel(Level.DEBUG);
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	public String[][] getExcelData(String filePath) throws IOException
	{
		String path = filePath;
		
		int rowCount = ExcelUtil.getRowCount(path, "Sheet1");
		int colCount = ExcelUtil.getCellCount(path, "Sheet1",1);
		
		String data[][] = new String[rowCount][colCount];
		
		for(int i=1; i<=rowCount; i++)
		{
			for(int j=0; j<colCount; j++)
			{
				data[i-1][j] = ExcelUtil.getCellData(path, "Sheet1", i, j);
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
		httpRequest.header("Authorization", "Bearer " + sessionId);

		response = httpRequest.request(Method.GET, RECENT_PHONE_NUMBER_PATH);
	}
	
	public void getCatalog(String sessionId, String phoneNumber) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("phone number:" + phoneNumber);

		JSONObject requestParams = new JSONObject();
		requestParams.put("phone", phoneNumber);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Authorization", "Bearer " + sessionId);
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, CATALOG_PATH);
	}
	
	public void createOrder(String sessionId, String phoneNumber, String catalogId) {
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
		httpRequest.header("Authorization", "Bearer " + sessionId);
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, ORDER_PATH);
	}
	
	public void cancelOrder(String sessionId, String transactionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("transaction id:" + transactionId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + sessionId);
		
		response = httpRequest.request(Method.DELETE, CANCEL_ORDER_PATH + transactionId);
	}
	
	public void payOrder(String sessionId, String transactionId, String paymentMethodId, String voucherId) {
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
		httpRequest.header("Authorization", "Bearer " + sessionId);
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
		httpRequest.header("Authorization", "Bearer " + sessionId);
		
		response = httpRequest.request(Method.GET, MY_VOUCHER_PATH + page);
	}
	
	public void getPromotionVoucher(String sessionId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + sessionId);
		
		response = httpRequest.request(Method.GET, PROMOTION_VOUCHER_PATH + page);
	}
	
	public void getRecommendationVoucher(String sessionId, String transactionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("transaction id:" + transactionId);
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("transactionId", transactionId);

		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + sessionId);
		
		response = httpRequest.request(Method.GET, RECOMMENDATION_VOUCHER_PATH);
	}
	
	public void getVoucherDetails(String sessionId, String voucherId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("voucher id:" + voucherId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + sessionId);
		
		response = httpRequest.request(Method.GET, VOUCHER_DETAILS_PATH + voucherId + "/detail");
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}

		return conn;
	}
}
