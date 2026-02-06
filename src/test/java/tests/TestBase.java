package tests;

import api.AppApi;
import api.MockApi;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import helpers.ApiConfig;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class TestBase {

    public static final ApiConfig config = ConfigFactory.create(ApiConfig.class);

    public static final String API_KEY = config.apiKey();
    public static final String ENDPOINT = "/endpoint";
    public static final String AUTH_PATH = "/auth";
    public static final String DO_ACTION_PATH = "/doAction";

    protected final AppApi appApi = new AppApi();
    protected final MockApi mockApi = new MockApi();
    protected final Faker faker = new Faker();
    private static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8888));

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = config.baseUrl();
        RestAssured.defaultParser = Parser.JSON;

        wireMockServer.start();
        configureFor("localhost", 8888);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetMocks() {
        reset();
    }
}
