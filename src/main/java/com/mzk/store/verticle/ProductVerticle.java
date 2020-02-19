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

	public static final int DEFAULT_PORT = 8080;	
	public static final int HTTP_SUCCESS = 200;
	public static final int HTTP_FAIL = 400;

	public static final JsonObject SUCCESS_MESSAGE_RESPONSE = new JsonObject().put("Status", "Success");
	public static final JsonObject ERROR_MESSAGE_RESPONSE = new JsonObject().put("Status", "Error");
	public static final String DEFAULT_ERROR_MESSAGE = "Unknown";

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
		vertx.createHttpServer().requestHandler(router).listen(config().getInteger("http.port", DEFAULT_PORT), result -> {

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
			response.setStatusCode(HTTP_FAIL)
					.end(ERROR_MESSAGE_RESPONSE.put("Message", config().getString("message.error.invalid", DEFAULT_ERROR_MESSAGE)).encodePrettily());
		} else {
			Product product = Json.decodeValue(productJson.toString(), Product.class);

			if (ProductHelper.isValidProduct(product)) {
				if (ProductHelper.productExists(product, productRepository)) {
					response.setStatusCode(HTTP_FAIL).end(
							ERROR_MESSAGE_RESPONSE.put("Message", config().getString("message.error.duplicated", DEFAULT_ERROR_MESSAGE)).encodePrettily());
				} else {
					productRepository.insert(product);
					response.setStatusCode(HTTP_SUCCESS).end(SUCCESS_MESSAGE_RESPONSE.encodePrettily());
				}
			} else {
				response.setStatusCode(HTTP_FAIL)
						.end(ERROR_MESSAGE_RESPONSE.put("Message", config().getString("message.error.invalid", DEFAULT_ERROR_MESSAGE)).encodePrettily());
			}

			response.end();
		}
	}

	private void removeProductsHandler(RoutingContext routingContext) {

		HttpServerResponse response = routingContext.response();
		JsonObject productJson = routingContext.getBodyAsJson();

		if (productJson == null) {
			response.setStatusCode(HTTP_FAIL)
					.end(ERROR_MESSAGE_RESPONSE.put("Message", config().getString("message.error.invalid", DEFAULT_ERROR_MESSAGE)).encodePrettily());
		} else {
			Product product = Json.decodeValue(productJson.toString(), Product.class);

			if (ProductHelper.isValidProduct(product)) {
				if (ProductHelper.productExists(product, productRepository)) {
					boolean productDeleted = productRepository.delete(product);

					if (productDeleted)
						response.setStatusCode(HTTP_SUCCESS).end(SUCCESS_MESSAGE_RESPONSE.encodePrettily());

				} else {
					response.setStatusCode(HTTP_FAIL)
							.end(ERROR_MESSAGE_RESPONSE.put("Message", config().getString("message.error.not_exist", DEFAULT_ERROR_MESSAGE)).encodePrettily());
				}
			} else {
				response.setStatusCode(HTTP_FAIL)
						.end(ERROR_MESSAGE_RESPONSE.put("Message", config().getString("message.error.invalid", DEFAULT_ERROR_MESSAGE)).encodePrettily());
			}

			response.end();
		}
	}

}
