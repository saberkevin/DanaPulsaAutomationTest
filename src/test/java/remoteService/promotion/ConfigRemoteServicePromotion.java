package remoteService.promotion;

public class ConfigRemoteServicePromotion {
	public static final String FILE_PATH = "../DanaPulsaAutomationTest/src/test/java/remoteService/promotion/RemoteServicePromotionTestData.xlsx";
	public static final String SHEET_GET_MY_VOUCHER = "Get My Vouchers";
	public static final String SHEET_GET_VOUCHER_PROMOTION = "Get Promotion Vouchers";
	public static final String SHEET_GET_VOUCHER_RECOMMENDATION = "Get Voucher Recommendation";
	public static final String SHEET_GET_VOUCHER_DETAILS = "Get Voucher Details";
	public static final String SHEET_REDEEM = "Redeem";
	public static final String SHEET_UNREDEEM = "Unredeem";
	public static final String SHEET_ISSUE_VOUCHER = "Voucher Details";
	public static final String SHEET_ELIGIBLE_TO_GET_VOUCHER = "Voucher Details";
	
	public static final String BASE_URI = "https://pulsa-voucher.herokuapp.com";
	public static final String ENDPOINT_PATH = "/test";
	public static final String QUEUE_GET_MY_VOUCHER = "getMyVoucher";
	public static final String QUEUE_GET_VOUCHER_PROMOTION  = "getVoucherPromotion";
	public static final String QUEUE_GET_VOUCHER_RECOMMENDATION = "getVoucherRecommendation";
	public static final String QUEUE_GET_VOUCHER_DETAILS = "getVoucherDetail";
	public static final String QUEUE_REDEEM = "redeem";
	public static final String QUEUE_UNREDEEM  = "unredeem";
	public static final String QUEUE_ISSUE_VOUCHER = "getProviderById";
	public static final String QUEUE_ELIGIBLE_TO_GET_VOUCHER= "getRecentNumber";
	
	public static final String USER_NAME = "Zanuar";
	public static final String USER_EMAIL = "triromadon@gmail.com";
	public static final String USER_USERNAME = "6281252930398";
	public static final int USER_PIN = 123456;
}
