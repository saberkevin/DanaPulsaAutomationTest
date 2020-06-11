package model;

import java.util.Date;

public class Voucher {
	private long id;
	private long typeId;
	private String name;
	private long discount;
	private long minPurchase;
	private boolean isActive;
	private long maxDeduction;
	private long value;
	private String filePath;
	private Date expiryDate;
	private String voucherTypeName;	
	private String paymentMethod;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTypeId() {
		return typeId;
	}
	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getDiscount() {
		return discount;
	}
	public void setDiscount(long discount) {
		this.discount = discount;
	}
	public long getMinPurchase() {
		return minPurchase;
	}
	public void setMinPurchase(long minPurchase) {
		this.minPurchase = minPurchase;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public long getMaxDeduction() {
		return maxDeduction;
	}
	public void setMaxDeduction(long maxDeduction) {
		this.maxDeduction = maxDeduction;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getVoucherTypeName() {
		return voucherTypeName;
	}
	public void setVoucherTypeName(String voucherTypeName) {
		this.voucherTypeName = voucherTypeName;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}
