package remoteService.order;

public class ConfigRemoteServiceOrder {
	public static final String FILE_PATH = "../DanaPulsaAutomationTest/src/test/java/remoteService/order/RemoteServiceOrderTestData.xlsx";
	public static final String SHEET_CANCEL = "Cancel";
	public static final String SHEET_CREATE_TRANSACTION = "Create Transaction";
	public static final String SHEET_GET_ALL_CATALOG = "Get All Catalog";
	public static final String SHEET_GET_HISTORY_COMPLETED = "Get History Completed";
	public static final String SHEET_GET_HISTORY_IN_PROGRESS = "Get History in Progress";
	public static final String SHEET_GET_PAYMENT_METHOD_NAME_BY_ID = "Get Payment Method Name By ID";
	public static final String SHEET_GET_PROVIDER_BY_ID = "Get Provider By ID";
	public static final String SHEET_GET_RECENT_NUMBER = "Get Recent Number";
	public static final String SHEET_GET_TRANSACTION_BY_ID = "Get Transaction By ID";
	public static final String SHEET_GET_TRANSACTION_BY_ID_BY_USER_ID = "Get Transaction By Id By UserId";
	public static final String SHEET_PAY = "Pay";
	
	public static final String BASE_URI = "https://debrief2-pulsa-order.herokuapp.com";
	public static final String ENDPOINT_PATH = "/api/test/";
	public static final String QUEUE_CANCEL = "cancel";
	public static final String QUEUE_CREATE_TRANSACTION = "createTransaction";
	public static final String QUEUE_GET_ALL_CATALOG = "getAllCatalog";
	public static final String QUEUE_GET_HISTORY_COMPLETED = "getHistoryCompleted";
	public static final String QUEUE_GET_HISTORY_IN_PROGRESS = "getHistoryInProgress";
	public static final String QUEUE_GET_PAYMENT_METHOD_NAME_BY_ID = "getPaymentMethodNameById";
	public static final String QUEUE_GET_PROVIDER_BY_ID = "getProviderById";
	public static final String QUEUE_GET_RECENT_NUMBER = "getRecentNumber";
	public static final String QUEUE_TRANSACTION_BY_ID = "getTransactionById";
	public static final String QUEUE_TRANSACTION_BY_ID_BY_USER_ID = "getTransactionByIdByUserId";
	public static final String QUEUE_PAY = "pay";
	
	public static final String USER_NAME = "Zanuar";
	public static final String USER_EMAIL = "triromadon@gmail.com";
	public static final String USER_USERNAME = "6281252930398";
	public static final int USER_PIN = 123456;
}
