package api;

import io.qameta.allure.Step;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static tests.TestBase.AUTH_PATH;
import static tests.TestBase.DO_ACTION_PATH;

public class MockApi {

    @Step("Mock: Внешний сервис вернет {status} для /auth")
    public void setupAuthResponse(int status) {
        stubFor(post(urlEqualTo(AUTH_PATH))
                .willReturn(aResponse().withStatus(status)));
    }

    @Step("Mock: Внешний сервис вернет {status} для /doAction")
    public void setupDoActionResponse(int status) {
        stubFor(post(urlEqualTo(DO_ACTION_PATH))
                .willReturn(aResponse().withStatus(status)));
    }

    @Step("Mock: Внешний сервис ответит {status} с задержкой {delay}мс")
    public void setupAuthResponseWithDelay(int status, int delay) {
        stubFor(post(urlEqualTo(AUTH_PATH))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withFixedDelay(delay)));
    }
}