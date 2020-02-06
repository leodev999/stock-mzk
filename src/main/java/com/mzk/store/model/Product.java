package com.mzk.store.model;

public class Product {

	private String serialNumber;
	private String barCode;
	private String name;

	public Product(String serialNumber, String barCode, String name) {
		super();
		this.serialNumber = serialNumber;
		this.barCode = barCode;
		this.name = name;
	}

	public Product() {
		super();
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getBarCode() {
		return barCode;
	}

	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
