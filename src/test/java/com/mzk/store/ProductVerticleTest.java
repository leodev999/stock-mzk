package com.mzk.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.mzk.store.verticle.ProductVerticle;

@ExtendWith(VertxExtension.class)
public class ProductVerticleTest {
	
	public static final int DEFAULT_PORT = 8080;
	public static final String DEFAULT_HOST = "localhost";

	@BeforeEach
	@DisplayName("Deploy a verticle")
	void prepare(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new ProductVerticle(), testContext.completing());
		testContext.completeNow();
	}

	@Test
	@DisplayName("Test Server Running")
	void testServerRunning(Vertx vertx, VertxTestContext testContext) {

		WebClient client = WebClient.create(vertx);

		client.get(DEFAULT_PORT, DEFAULT_HOST, "/").as(BodyCodec.string())
				.send(testContext.succeeding(response -> testContext.verify(() -> {
					assertEquals(response.body(), "Verticle is Running");
					testContext.completeNow();
				})));
	}

	@Test
	@DisplayName("Test Inserting a Product with new serialNumber and barCode")
	void testNewProduct(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject(
				"{\"serialNumber\": \"123\", \"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						assertThat(response.statusCode()).isEqualTo(200);
						assertThat(response.bodyAsJsonObject().getValue("Status")).isEqualTo("Success");
						testContext.completeNow();
					});
				}));
	}

	@Test
	@DisplayName("Test Inserting a Product with existing serialNumber and barCode")
	void testNewExistingProduct(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject(
				"{\"serialNumber\": \"123\", \"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products")
								.putHeader("content-type", "application-json; charset=utf-8")
								.sendJson(newProduct, testContext.succeeding(response2 -> {
									testContext.verify(() -> {
										assertThat(response2.statusCode()).isEqualTo(400);
										assertThat(response2.bodyAsJsonObject().getValue("Status")).isEqualTo("Error");
										testContext.completeNow();
									});
								}));
					});
				}));
	}

	@Test
	@DisplayName("Test Inserting a invalid Product (missing serialNumber)")
	void testInvalidProductSerialNumber(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject("{\"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						assertThat(response.statusCode()).isEqualTo(400);
						assertThat(response.bodyAsJsonObject().getValue("Status")).isEqualTo("Error");
						testContext.completeNow();
					});
				}));
	}

	@Test
	@DisplayName("Test Inserting a invalid Product (missing barCode)")
	void testInvalidProductBarCode(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject("{\"serialNumber\": \"123\", \"name\": \"Camiseta Branca\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						assertThat(response.statusCode()).isEqualTo(400);
						assertThat(response.bodyAsJsonObject().getValue("Status")).isEqualTo("Error");
						testContext.completeNow();
					});
				}));
	}

	@Test
	@DisplayName("Test Inserting a invalid Product (missing name)")
	void testInvalidProductName(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject("{\"serialNumber\": \"123\", \"barCode\": \"456456456654\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						assertThat(response.statusCode()).isEqualTo(400);
						assertThat(response.bodyAsJsonObject().getValue("Status")).isEqualTo("Error");
						testContext.completeNow();
					});
				}));
	}

	@Test
	@DisplayName("Test Inserting a Product (empty fields)")
	void testEmptyVariables(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject("{\"serialNumber\": \"\", \"barCode\": \"\",\"name\":\"\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						assertThat(response.statusCode()).isEqualTo(400);
						assertThat(response.bodyAsJsonObject().getValue("Status")).isEqualTo("Error");
						testContext.completeNow();
					});
				}));
	}
	
	@Test
	@DisplayName("Test deleting an existing product.")
	void testDeletingProduct(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject(
				"{\"serialNumber\": \"123\", \"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						client.delete(DEFAULT_PORT, DEFAULT_HOST, "/api/products")
								.putHeader("content-type", "application-json; charset=utf-8")
								.sendJson(newProduct, testContext.succeeding(response2 -> {
									testContext.verify(() -> {
										assertThat(response2.statusCode()).isEqualTo(200);
										assertThat(response2.bodyAsJsonObject().getValue("Status")).isEqualTo("Success");
										testContext.completeNow();
									});
								}));
					});
				}));
	}
	
	@Test
	@DisplayName("Test deleting a non-existent Product")
	void testDeletingNonExistingProduct(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject(
				"{\"serialNumber\": \"123\", \"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");
		
		JsonObject newProduct2 = new JsonObject(
				"{\"serialNumber\": \"1234\", \"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						client.delete(DEFAULT_PORT, DEFAULT_HOST, "/api/products")
								.putHeader("content-type", "application-json; charset=utf-8")
								.sendJson(newProduct2, testContext.succeeding(response2 -> {
									testContext.verify(() -> {
										assertThat(response2.statusCode()).isEqualTo(400);
										assertThat(response2.bodyAsJsonObject().getValue("Status")).isEqualTo("Error");
										testContext.completeNow();
									});
								}));
					});
				}));
	}
	
	@Test
	@DisplayName("Test deleting an invalid Product")
	void testDeletingInvalidProduct(Vertx vertx, VertxTestContext testContext) {

		JsonObject newProduct = new JsonObject(
				"{\"serialNumber\": \"123\", \"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");
		
		JsonObject newProduct2 = new JsonObject(
				"{\"barCode\": \"456456456654\", \"name\": \"Camiseta Branca\"}");

		WebClient client = WebClient.create(vertx);

		client.post(DEFAULT_PORT, DEFAULT_HOST, "/api/products").putHeader("content-type", "application-json; charset=utf-8")
				.sendJson(newProduct, testContext.succeeding(response -> {
					testContext.verify(() -> {
						client.delete(DEFAULT_PORT, DEFAULT_HOST, "/api/products")
								.putHeader("content-type", "application-json; charset=utf-8")
								.sendJson(newProduct2, testContext.succeeding(response2 -> {
									testContext.verify(() -> {
										assertThat(response2.statusCode()).isEqualTo(400);
										assertThat(response2.bodyAsJsonObject().getValue("Status")).isEqualTo("Error");
										testContext.completeNow();
									});
								}));
					});
				}));
	}

	@AfterEach
	@DisplayName("Check that the verticle is still there")
	void lastChecks(Vertx vertx) {
		vertx.close();
	}

}