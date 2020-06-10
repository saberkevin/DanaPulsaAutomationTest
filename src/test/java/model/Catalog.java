package model;

public class Catalog {
	private String id;
	private Provider provider;
	private long value;
	private long price;
		
	public Catalog() {
		this.id = "";
		this.provider = null;
		this.value = 0;
		this.price = 0;
	}
	
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

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}	
}
