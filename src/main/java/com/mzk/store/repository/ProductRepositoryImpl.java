package com.mzk.store.repository;

import com.mzk.store.model.Product;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/*
 * 
 * @author <a href="https://github.com/leomayerr/">Leonardo Mayer</a>
 */
public class ProductRepositoryImpl implements ProductRepository {
	
	private JsonArray products;
	
	public ProductRepositoryImpl() {
		products = new JsonArray();
	}
	
	@Override
	public void insert(Product product) {		
		products.add(JsonObject.mapFrom(product));
	}
	
	@Override
	public boolean delete(Product product) {

		for (int i = 0; i < products.size(); ++i) {

			JsonObject json = products.getJsonObject(i);

			Product productStocked = Json.decodeValue(json.toString(), Product.class);

			if (product.getBarCode().equals(productStocked.getBarCode())
					&& product.getSerialNumber().equals(productStocked.getSerialNumber())) {
				products.remove(i);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public JsonArray list() {
		return this.products;
	}
	
}
