package com.mzk.store.repository;

import com.mzk.store.model.Product;

import io.vertx.core.json.JsonArray;

public interface ProductRepository {

	public void insert(Product product);

	public boolean delete(Product product);

	public JsonArray list();

}
