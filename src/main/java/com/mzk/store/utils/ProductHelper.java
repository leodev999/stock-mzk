package com.mzk.store.utils;

import com.mzk.store.model.Product;
import com.mzk.store.repository.ProductRepository;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/*
 * 
 * @author <a href="https://github.com/leomayerr/">Leonardo Mayer</a>
 */
public class ProductHelper {

	public static void loadSampleData(ProductRepository productRepository) {

		productRepository.insert(new Product("1", "7898392930332", "Camisa Polo Azul Marinho"));
		productRepository.insert(new Product("2", "7898392930332", "Camisa Polo Azul Marinho"));
		productRepository.insert(new Product("3", "7898392930332", "Camisa Polo Azul Marinho"));
		productRepository.insert(new Product("1", "5280001427920", "Regata Masculina B-01 MXD"));
		productRepository.insert(new Product("1", "1466571884344", "Bermuda Sarja Slim"));
	}

	public static boolean isValidProduct(Product product) {

		if (product != null) {
			if (product.getBarCode() != null && product.getSerialNumber() != null && product.getName() != null) {
				if (product.getBarCode().length() > 0 && product.getSerialNumber().length() > 0
						&& product.getName().length() > 0) {

					try {
						Double.parseDouble(product.getBarCode());
						Double.parseDouble(product.getSerialNumber());
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
				}
			}
		}
		return false;
	}

	public static boolean productExists(Product product, ProductRepository productRepository) {

		for (int i = 0; i < productRepository.list().size(); ++i) {

			JsonObject json = productRepository.list().getJsonObject(i);
			Product productStocked = Json.decodeValue(json.toString(), Product.class);

			if (product.getBarCode().equals(productStocked.getBarCode())
					&& product.getSerialNumber().equals(productStocked.getSerialNumber())) {
				return true;
			}
		}
		return false;
	}

}
