package model;

import java.util.Date;

public class Voucher {
	private String id;
	private String name;
	private String voucherTypeName;
	private String filePath;
	private String paymentMethod;
	private int discount;
	private int maximumDeduction;
	private int minimumPurchase;
	private Date expiredDate;
	private String term;
	private String condition;
	private String instruction;

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

	public int getDiscount() {
		return discount;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}

	public int getMaximumDeduction() {
		return maximumDeduction;
	}

	public void setMaximumDeduction(int maximumDeduction) {
		this.maximumDeduction = maximumDeduction;
	}

	public int getMinimumPurchase() {
		return minimumPurchase;
	}

	public void setMinimumPurchase(int minimumPurchase) {
		this.minimumPurchase = minimumPurchase;
	}

	public Date getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
}
