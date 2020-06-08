package base;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
import model.Catalog;
import model.Transaction;
import model.User;
import model.Voucher;
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
	private static final String LOGUT_PATH = "/api/logout";
	
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
			
			String dbUrl = "";					
			String username = "";	
			String password = "";
			
			con = DriverManager.getConnection(dbUrl,username,password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
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
		requestParams.put("password", pin);
		
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
		
		requestParams.put("id", Integer.parseInt(id));
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
		
		requestParams.put("id", Integer.parseInt(id));
		
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
		
		requestParams.put("id", Integer.parseInt(id));
		requestParams.put("code", Integer.parseInt(code));
		
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
	
	public void historyInProgress(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, HISTORY_IN_PROGRESS_PATH+id);
	}
	
	public void historyCompleted(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, HISTORY_COMPLETED_PATH+id);
	}
	
	public void historyDetail(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, HISTORY_DETAILS_PATH+id);
	}
	
	public void getRecentPhoneNumber(User user) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());

		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Authorization", "Bearer " + user.getToken());

		response = httpRequest.request(Method.GET, RECENT_PHONE_NUMBER_PATH);
	}
	
	public void getCatalog(User user, String phoneNumber) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("phone number:" + phoneNumber);

		JSONObject requestParams = new JSONObject();
		requestParams.put("phone", phoneNumber);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.GET, CATALOG_PATH);
	}
	
	public void createOrder(User user, String phoneNumber, Catalog catalog) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("phone number:" + phoneNumber);
		logger.info("catalog id:" + catalog.getId());
				
		JSONObject requestParams = new JSONObject();
		requestParams.put("phone", phoneNumber);
		requestParams.put("catalogId", catalog.getId());
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, ORDER_PATH);
	}
	
	public void cancelOrder(User user, Transaction transaction) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("transaction id:" + transaction.getId());
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		
		response = httpRequest.request(Method.DELETE, CANCEL_ORDER_PATH + transaction.getId());
	}
	
	public void payOrder(User user, Transaction transaction, String paymentMethodId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("transaction id:" + transaction.getId());
		logger.info("payment method id:" + paymentMethodId);
		logger.info("voucher id:" + transaction.getVoucher().getId());
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("transactionId", transaction.getId());
		requestParams.put("methodId", paymentMethodId);
		requestParams.put("voucherId", transaction.getVoucher().getId());
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		httpRequest.header("Content-Type", "application/json");
		
		response = httpRequest.request(Method.POST, PAYMENT_PATH);
	}
	
	public void getmyVoucher(User user, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		
		response = httpRequest.request(Method.GET, MY_VOUCHER_PATH + page);
	}
	
	public void getPromotionVoucher(User user, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		
		response = httpRequest.request(Method.GET, PROMOTION_VOUCHER_PATH + page);
	}
	
	public void getRecommendationVoucher(User user, Transaction transaction) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("transaction id:" + transaction.getId());
		
		JSONObject requestParams = new JSONObject();
		requestParams.put("transactionId", transaction.getId());

		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		
		response = httpRequest.request(Method.GET, RECOMMENDATION_VOUCHER_PATH);
	}
	
	public void getVoucherDetails(User user, Voucher voucher) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + user.getToken());
		logger.info("voucher id:" + voucher.getId());
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Authorization", "Bearer " + user.getToken());
		
		response = httpRequest.request(Method.GET, VOUCHER_DETAILS_PATH + voucher.getId() + "/detail");
	}
}
