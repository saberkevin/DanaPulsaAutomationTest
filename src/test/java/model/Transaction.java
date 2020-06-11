package model;

import java.util.Date;

public class Transaction {
	private long id;
	private long userId;
	private long methodId;
	private String phoneNumber;
	private long catalogId;
	private long voucherId;
	private long statusId;
	private Date createdAt;
	private Date updatedAt;
	private String status;
	private String paymentMethodName;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getMethodId() {
		return methodId;
	}
	public void setMethodId(long methodId) {
		this.methodId = methodId;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public long getCatalogId() {
		return catalogId;
	}
	public void setCatalogId(long catalogId) {
		this.catalogId = catalogId;
	}
	public long getVoucherId() {
		return voucherId;
	}
	public void setVoucherId(long voucherId) {
		this.voucherId = voucherId;
	}
	public long getStatusId() {
		return statusId;
	}
	public void setStatusId(long statusId) {
		this.statusId = statusId;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPaymentMethodName() {
		return paymentMethodName;
	}
	public void setPaymentMethodName(String paymentMethodName) {
		this.paymentMethodName = paymentMethodName;
	}
}
