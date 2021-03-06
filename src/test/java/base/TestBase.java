package base;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.RpcClientParams;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.User;
import utilities.ExcelUtil;

@SuppressWarnings("unchecked")
public class TestBase {
	private static final String REGISTER_PATH = "/api/register";
	private static final String LOGIN_PATH = "/api/login/";
	private static final String VERIFY_PIN_LOGIN_PATH = "/api/verifypin-login";
	private static final String FORGOT_PIN_OTP_PATH = "/api/forgotpin-otp";
	private static final String CHANGE_PIN_OTP_PATH = "/api/changepin-otp";
	private static final String VERIFY_OTP_PATH = "/api/verify-otp";
	private static final String GET_OTP_PATH = "/api/otp/";
	private static final String CHANGE_PIN_PATH = "/api/change-pin";
	private static final String GET_PROFILE_PATH = "/api/profile";
	private static final String GET_BALANCE_PATH = "/api/balance";
	private static final String LOGOUT_PATH = "/api/logout";
	private static final String RECENT_PHONE_NUMBER_PATH = "/api/recent-number";
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

	public String URI = "http://debrief.herokuapp.com";
	public String URIOrder = "https://debrief2-pulsa-order.herokuapp.com";
	public String URIOrderBackup = "https://debrief2-pulsa-order-backup.herokuapp.com";
	public String URIPromotion = "https://pulsa-voucher.herokuapp.com";
	public String memberURI = "https://member-domain.herokuapp.com/member";
	public String memberAMQP = "amqp://ynjauqav:K83KvUARdw7DyYLJF2_gt2RVzO-NS2YM@lively-peacock.rmq.cloudamqp.com/ynjauqav";
	public String orderAMQP = "amqp://ynjauqav:K83KvUARdw7DyYLJF2_gt2RVzO-NS2YM@lively-peacock.rmq.cloudamqp.com/ynjauqav";
	public String promotionAMQP = "amqp://ynjauqav:K83KvUARdw7DyYLJF2_gt2RVzO-NS2YM@lively-peacock.rmq.cloudamqp.com/ynjauqav";
	public String excelPrefix = "../DanaPulsaAutomationTest/src/test/java/";
	public Logger logger;
	
	@BeforeClass
	public void setup()
	{
		logger = Logger.getLogger("restAPI");;
		PropertyConfigurator.configure("src/Log4j.properties");
		logger.setLevel(Level.DEBUG);
	}
	
	public String setSession(String userId)
	{
		String pinForSession = "";
		
		String query = "SELECT id, pin FROM user\n" + 
				"WHERE id = ?";
		
		Map<String, Object> param = new LinkedHashMap<String, Object>();
		param.put("id", userId);
		List<Map<String, Object>> responseResult = sqlExec(query, param, "MEMBER");

		for (Map<String, Object> result : responseResult) 
		{
			pinForSession = result.get("pin").toString();
		}		
		verifyPinLogin(userId, pinForSession);
		return response.getCookie("JSESSIONID");
	}
	
	public String callRP(String url, String routingKey, String message) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		try {
			URI rabbitMqUrl = new URI(url);
			ConnectionFactory factory = new ConnectionFactory();
		    factory.setUsername(rabbitMqUrl.getUserInfo().split(":")[0]);
		    factory.setPassword(rabbitMqUrl.getUserInfo().split(":")[1]);
		    factory.setHost(rabbitMqUrl.getHost());
		    factory.setPort(rabbitMqUrl.getPort());
		    factory.setVirtualHost(rabbitMqUrl.getPath().substring(1));
		    com.rabbitmq.client.Connection connection = factory.newConnection();
		    RpcClientParams params = new RpcClientParams();
		    params.channel(connection.createChannel());
		    params.exchange("");
		    params.routingKey(routingKey);
		    params.timeout(20000);
		    return new RpcClient(params).stringCall(message);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return "Error in try catch!";
	  }
	
	public void persistentCall(String url, String routingKey, String message) {
		try {
		  final URI rabbitMqUrl = new URI(url);
		  ConnectionFactory factory = new ConnectionFactory();
		  factory.setUsername(rabbitMqUrl.getUserInfo().split(":")[0]);
		  factory.setPassword(rabbitMqUrl.getUserInfo().split(":")[1]);
		  factory.setHost(rabbitMqUrl.getHost());
		  factory.setPort(rabbitMqUrl.getPort());
		  factory.setVirtualHost(rabbitMqUrl.getPath().substring(1));
		  com.rabbitmq.client.Connection connection = factory.newConnection();
		  com.rabbitmq.client.Channel channel = connection.createChannel();
		  channel.queueDeclare(routingKey, true, false, false, null);
		  channel.basicPublish("", routingKey,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes(StandardCharsets.UTF_8));
		  logger.info(message+" sent to "+routingKey);
		} catch (URISyntaxException | IOException | TimeoutException e) {
			e.printStackTrace();
	    }
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
	
	public void checkStatusCode(int sc)
	{
		logger.info("***** Check Status Code *****");
		
		int statusCode = response.getStatusCode();
		logger.info("Status Code = " + statusCode);
		Assert.assertEquals(statusCode, sc);	
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
	
	public void checkCodeValid(String code)
	{
		String regex = "^[0-9]{4}$";
		 
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(code);
		
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
		requestParams.put("pin", pin);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, REGISTER_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void login(String phone) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("phone:" + phone);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, LOGIN_PATH + phone);
		logger.info(response.getBody().asString());
	}
	
	public void verifyPinLogin(String id, String pin) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("pin:" + pin);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("id", id);
		requestParams.put("pin", pin);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, VERIFY_PIN_LOGIN_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void getProfile(String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, GET_PROFILE_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void getBalance(String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, GET_BALANCE_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void changePinOtp(String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.POST, CHANGE_PIN_OTP_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void forgotPinOtp(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("id", id);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, FORGOT_PIN_OTP_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void verifyOtp(String id, String code) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		logger.info("code:" + code);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("id", id);
		requestParams.put("code", code);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, VERIFY_OTP_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void changePin(String pin, String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("pin:" + pin);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		JSONObject requestParams = new JSONObject();
		
		requestParams.put("pin", pin);
		
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.PUT, CHANGE_PIN_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void GetOtp(String id) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		
		response = httpRequest.request(Method.GET, GET_OTP_PATH+id);
		logger.info(response.getBody().asString());
	}
	
	public void logout(String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.DELETE, LOGOUT_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void historyInProgress(String page, String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, HISTORY_IN_PROGRESS_PATH+page);
		logger.info(response.getBody().asString());
	}
	
	public void historyCompleted(String page, String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, HISTORY_COMPLETED_PATH+page);
		logger.info(response.getBody().asString());
	}
	
	public void historyDetail(String id, String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("id:" + id);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, HISTORY_DETAILS_PATH+id);
		logger.info(response.getBody().asString());
	}
	
	public void getRecentPhoneNumber(String sessionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);

		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);

		response = httpRequest.request(Method.GET, RECENT_PHONE_NUMBER_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void getCatalog(String sessionId, String phoneNumber) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("phone number:" + phoneNumber);

		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, CATALOG_PATH + phoneNumber);
		logger.info(response.getBody().asString());
	}
	
	public void createOrder(String sessionId, String phoneNumber, String catalogId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("phone number:" + phoneNumber);
		logger.info("catalog id:" + catalogId);
				
		JSONObject requestParams = new JSONObject();
		requestParams.put("phoneNumber", phoneNumber);
		requestParams.put("catalogId", catalogId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, ORDER_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void cancelOrder(String sessionId, String transactionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("transaction id:" + transactionId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.DELETE, CANCEL_ORDER_PATH + transactionId);
		logger.info(response.getBody().asString());
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
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		httpRequest.header("Content-Type", "application/json");
		httpRequest.body(requestParams.toJSONString());
		
		response = httpRequest.request(Method.POST, PAYMENT_PATH);
		logger.info(response.getBody().asString());
	}
	
	public void getMyVoucher(String sessionId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, MY_VOUCHER_PATH + page);
		logger.info(response.getBody().asString());
	}
	
	public void getPromotionVoucher(String sessionId, String page) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("page:" + page);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);

		response = httpRequest.request(Method.GET, PROMOTION_VOUCHER_PATH + page);
		logger.info(response.getBody().asString());
	}
	
	public void getRecommendationVoucher(String sessionId, String transactionId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("transaction id:" + transactionId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);
		
		response = httpRequest.request(Method.GET, RECOMMENDATION_VOUCHER_PATH + transactionId);
		logger.info(response.getBody().asString());
	}
	
	public void getVoucherDetails(String sessionId, String voucherId) {
		logger.info("***** Started " + this.getClass().getSimpleName() + " *****");
		logger.info("Test Data: ");
		logger.info("session id:" + sessionId);
		logger.info("voucher id:" + voucherId);
		
		RestAssured.baseURI = URI;
		httpRequest = RestAssured.given();		
		httpRequest.header("Cookie", "JSESSIONID=" + sessionId);

		response = httpRequest.request(Method.GET, VOUCHER_DETAILS_PATH + voucherId);
		logger.info(response.getBody().asString());
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
		String dbUrl = "jdbc:mysql://remotemysql.com:3306/orGoW5vfGH";					
		String username = "orGoW5vfGH";	
		String password = "hesicA9XSO";
		
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
	
	public Connection setConnection(String service)
	{
		Connection conn = null;
		
		if(service.equalsIgnoreCase("MEMBER"))
		{
			conn = getConnectionMember();
		}
		else if(service.equalsIgnoreCase("ORDER"))
		{
			conn = getConnectionOrder();
		}
		else if(service.equalsIgnoreCase("PROMOTION"))
		{
			conn = getConnectionPromotion();
		}
		
		return conn;
	}
	
	//============ Some Command to DB ==============================//
	
	public void createUser(User user) {
		Connection conn = null;
		try {
			conn = setConnection("MEMBER");
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
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void deleteUserById(long id) {
		Connection conn = null;
		try {
			conn = setConnection("MEMBER");
			String query = "DELETE FROM user WHERE id = ?";

			PreparedStatement ps = conn.prepareStatement(query);			
			ps.setLong(1, id);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void deleteUserIfExist(String email, String username) {
		boolean userExist = false;
		Connection conn = null;
		
		try {
			conn = setConnection("MEMBER");
			String query = "SELECT * FROM user WHERE email = ? OR username = ?";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, email);
			ps.setString(2, username);

			ResultSet rs = ps.executeQuery();
			if (rs.next())
				userExist = true;

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		if (userExist) {
			try {
				conn = setConnection("MEMBER");
				String query = "DELETE FROM user WHERE email = ? OR username = ?";

				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(1, email);
				ps.setString(2, username);
				ps.executeUpdate();

				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
			        if (conn != null)
			            conn.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public void deleteUserByEmailAndUsername(String email, String username) {
		Connection conn = null;
		try {
			conn = setConnection("MEMBER");
			String queryString = "DELETE FROM user WHERE email = ? AND username = ?";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setString(1, email);
			ps.setString(2, username);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public long getUserIdByUsername(String username) {
		long id = 0;
		Connection conn = null;
		
		try {
			conn = setConnection("MEMBER");
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
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		return id;
	}
	
	public void createTransaction(long userId, String phoneNumber, long catalogId) {
		Connection conn = null;
		try {
			conn = setConnection("ORDER");
			String queryString = "INSERT INTO transaction(userId, methodId, phoneNumber, catalogId, statusId) VALUES (?, ?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, userId);
			ps.setLong(2, 1);
			ps.setString(3, phoneNumber);
			ps.setLong(4, catalogId);
			ps.setLong(5, 1);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void createTransaction(long userId, String phoneNumber, long catalogId, long statusId) {
		Connection conn = null;
		try {
			conn = setConnection("ORDER");
			String queryString = "INSERT INTO transaction(userId, methodId, phoneNumber, catalogId, statusId) VALUES (?, ?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, userId);
			ps.setLong(2, 1);
			ps.setString(3, phoneNumber);
			ps.setLong(4, catalogId);
			ps.setLong(5, statusId);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void deleteTransactionByPhoneNumber(String phoneNumber) {
		Connection conn = null;
		try {
			conn = setConnection("ORDER");
			String queryString = "DELETE FROM transaction WHERE phoneNumber = ?";

			PreparedStatement ps = conn.prepareStatement(queryString);			
			ps.setString(1, phoneNumber);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void deleteTransactionByUserId(long userId) {
		Connection conn = null;
		try {
			conn = setConnection("ORDER");
			String queryString = "DELETE FROM transaction WHERE userId = ?";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, userId);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public long getTransactionIdByUserId(long userId) {
		long id = 0;
		Connection conn = null;
		try {
			conn = setConnection("ORDER");
			String queryString = "SELECT id FROM transaction WHERE userId = ?";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, userId);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				id = rs.getLong("id");
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		return id;
	}
	
	public void createBalance(long userId, long balance) {
		Connection conn = null;
		try {
			conn = setConnection("MEMBER");
			String queryString = "INSERT INTO balance(userId, balance) VALUES(?, ?)";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, userId);
			ps.setLong(2, balance);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void updateBalance(long userId, long balance) {
		Connection conn = null;
		try {
			conn = setConnection("MEMBER");
			String queryString = "UPDATE balance SET balance = ? WHERE userId = ?";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, balance);
			ps.setLong(2, userId);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void deleteBalanceByUserId(long userId) {
		Connection conn = null;
		try {
			conn = setConnection("MEMBER");
			String queryString = "DELETE FROM balance WHERE userId = ?";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, userId);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}	
	}
	
	public void deleteBalanceByEmailByUsername(String email, String username) {
		Connection conn = null;
		try {
			conn = setConnection("MEMBER");
			String queryString = "DELETE FROM balance WHERE userId in (select id from user where email = ? or username = ?)";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setString(1, email);
			ps.setString(2, username);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void createUserVoucher(long userId, long voucherId, long voucherStatusId) {
		Connection conn = null;
		Date date = new Date();
		try {
			conn = setConnection("PROMOTION");
			String query = "INSERT INTO user_voucher(userId, voucherId, voucherStatusId, createdAt) VALUES(?, ?, ?, ?)";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setLong(1, userId);
			ps.setLong(2, voucherId);
			ps.setLong(3, voucherStatusId);
			ps.setDate(4, new java.sql.Date(date.getTime()));
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void deleteUserVoucherByUserId(long userId) {
		Connection conn = null;
		try {
			conn = setConnection("PROMOTION");
			String queryString = "DELETE FROM user_voucher WHERE userId = ?";

			PreparedStatement ps = conn.prepareStatement(queryString);
			ps.setLong(1, userId);
			ps.executeUpdate();

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}	
	}
	
	public List<Map<String, Object>> sqlExec(String query, Map<String, Object> param, String domain) {
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		Connection conn = null;
				
		try {
			conn = setConnection(domain);
			PreparedStatement ps = conn.prepareStatement(query);
			
			int index = 0;
			for (Map.Entry<String, Object> data: param.entrySet()) {
				index++;
				if (data.getValue() instanceof String) ps.setString(index, (String) data.getValue());
				else if (data.getValue() instanceof Integer) ps.setInt(index, (Integer) data.getValue());
				else if (data.getValue() instanceof Long) ps.setLong(index, (Long) data.getValue());
			}

			ResultSet rs = ps.executeQuery();
			ResultSetMetaData md = rs.getMetaData();
						
			while (rs.next()) {
				Map<String, Object> row = new HashMap<String, Object>(md.getColumnCount());
				
		        for(int i = 1; i <= md.getColumnCount(); ++i) {
		            row.put(md.getColumnLabel(i), rs.getObject(i));
		        }
		        
		        result.add(row);
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
		        if (conn != null)
		            conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		return result;
	}
}
