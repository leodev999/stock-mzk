package com.mzk.store.verticle;

import com.mzk.store.model.Product;
import com.mzk.store.repository.ProductRepository;
import com.mzk.store.repository.ProductRepositoryImpl;
import com.mzk.store.utils.ProductHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/*
 * 
 * @author <a href="https://github.com/leomayerr/">Leonardo Mayer</a>
 */
public class ProductVerticle extends AbstractVerticle {

	public static final int PORT = 8080;
	public static final int HTTP_STATUS_CODE_400 = 400;
	public static final int HTTP_STATUS_CODE_200 = 200;
	public static final String PRODUCT_VERTICLE_DOCUMENTATION = "https://www.google.com";

	public static final JsonObject SUCCESS_MESSAGE_RESPONSE = new JsonObject().put("Status", "Success");
	public static final JsonObject ERROR_MESSAGE_RESPONSE = new JsonObject().put("Status", "Error");

	public static final String ERROR_MESSAGE_DUPLICATED_VALUE = "Product already exists.";
	public static final String ERROR_MESSAGE_NOTEXISTS_VALUE = "Product does not exist.";
	public static final String ERROR_MESSAGE_INVALID_VALUE = "Invalid product.";

	ProductRepository productRepository = new ProductRepositoryImpl();

	@Override
	public void start(Promise<Void> promise) {

		ProductHelper.loadSampleData(productRepository);

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.get("/").handler(this::mainPageHandler);
		router.get("/api/products").handler(this::listProductsHandler);
		router.post("/api/products").handler(this::addProductsHandler);
		router.delete("/api/products").handler(this::removeProductsHandler);
		vertx.createHttpServer().requestHandler(router).listen(PORT, result -> {

			if (result.succeeded()) {
				promise.complete();
			} else {
				promise.fail(result.cause());
			}
		});
	}

	private void mainPageHandler(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "text/html")
				.end("Verticle is Running");
	}

	private void listProductsHandler(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json")
				.end(productRepository.list().encodePrettily());
	}

	private void addProductsHandler(RoutingContext routingContext) {

		HttpServerResponse response = routingContext.response();
		JsonObject productJson = routingContext.getBodyAsJson();

		if (productJson == null) {
			response.setStatusCode(HTTP_STATUS_CODE_400)
					.end(ERROR_MESSAGE_RESPONSE.put("Message", ERROR_MESSAGE_INVALID_VALUE).encodePrettily());
		} else {
			Product product = Json.decodeValue(productJson.toString(), Product.class);

			if (ProductHelper.isValidProduct(product)) {
				if (ProductHelper.productExists(product, productRepository)) {
					response.setStatusCode(HTTP_STATUS_CODE_400).end(
							ERROR_MESSAGE_RESPONSE.put("Message", ERROR_MESSAGE_DUPLICATED_VALUE).encodePrettily());
				} else {
					productRepository.insert(product);
					response.setStatusCode(HTTP_STATUS_CODE_200).end(SUCCESS_MESSAGE_RESPONSE.encodePrettily());
				}
			} else {
				response.setStatusCode(HTTP_STATUS_CODE_400)
						.end(ERROR_MESSAGE_RESPONSE.put("Message", ERROR_MESSAGE_INVALID_VALUE).encodePrettily());
			}

			response.end();
		}
	}

	private void removeProductsHandler(RoutingContext routingContext) {

		HttpServerResponse response = routingContext.response();
		JsonObject productJson = routingContext.getBodyAsJson();

		if (productJson == null) {
			response.setStatusCode(HTTP_STATUS_CODE_400)
					.end(ERROR_MESSAGE_RESPONSE.put("Message", ERROR_MESSAGE_INVALID_VALUE).encodePrettily());
		} else {
			Product product = Json.decodeValue(productJson.toString(), Product.class);

			if (ProductHelper.isValidProduct(product)) {
				if (ProductHelper.productExists(product, productRepository)) {
					boolean productDeleted = productRepository.delete(product);

					if (productDeleted)
						response.setStatusCode(HTTP_STATUS_CODE_200).end(SUCCESS_MESSAGE_RESPONSE.encodePrettily());

				} else {
					response.setStatusCode(HTTP_STATUS_CODE_400)
							.end(ERROR_MESSAGE_RESPONSE.put("Message", ERROR_MESSAGE_NOTEXISTS_VALUE).encodePrettily());
				}
			} else {
				response.setStatusCode(HTTP_STATUS_CODE_400)
						.end(ERROR_MESSAGE_RESPONSE.put("Message", ERROR_MESSAGE_INVALID_VALUE).encodePrettily());
			}

			response.end();
		}
	}

}
