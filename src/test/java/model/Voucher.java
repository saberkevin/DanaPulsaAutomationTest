package model;

import java.util.Date;

public class Voucher {
	private String id;
	private String name;
	private String voucherTypeName;
	private String filePath;
	private String paymentMethod;
	private long discount;
	private long maxDeduction;
	private long minPurchase;
	private Date expiryDate;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVoucherTypeName() {
		return voucherTypeName;
	}
	public void setVoucherTypeName(String voucherTypeName) {
		this.voucherTypeName = voucherTypeName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public long getDiscount() {
		return discount;
	}
	public void setDiscount(long discount) {
		this.discount = discount;
	}
	public long getMaxDeduction() {
		return maxDeduction;
	}
	public void setMaxDeduction(long maxiDeduction) {
		this.maxDeduction = maxiDeduction;
	}
	public long getMinPurchase() {
		return minPurchase;
	}
	public void setMinPurchase(long minPurchase) {
		this.minPurchase = minPurchase;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
}
