package model;

public class Catalog {
	private String id;
	private Provider provider;
	private int value;
	private int price;
		
	public Catalog(String id, Provider provider, int value, int price) {
		this.id = id;
		this.provider = provider;
		this.value = value;
		this.price = price;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}	
}
