package api;

import io.qameta.allure.Step;
import models.UserAction;
import models.UserResponse;
import specs.ResponseSpecs;
import tests.TestBase;

import static io.restassured.RestAssured.given;
import static specs.RequestSpecs.*;

public class AppApi {

    @Step("Запрос к приложению: action={action}, token={token}")
    public UserResponse executeAction(String token, UserAction action, int expectedStatus) {
        return given(baseRequestSpec)
                .formParam("token", token)
                .formParam("action", action.getValue())
                .when()
                .post(TestBase.ENDPOINT)
                .then()
                .spec(ResponseSpecs.responseSpec(expectedStatus))
                .extract().as(UserResponse.class);
    }

    @Step("Запрос с кастомным API-ключом: action={action}, apiKey={apiKey}")
    public UserResponse executeActionWithCustomKey(String token, UserAction action, int expectedStatus, String apiKey) {
        return given(noHeaderSpec)
                .header("X-Api-Key", apiKey)
                .formParam("token", token)
                .formParam("action", action.getValue())
                .when()
                .post(TestBase.ENDPOINT)
                .then()
                .spec(ResponseSpecs.responseSpec(expectedStatus))
                .extract().as(UserResponse.class);
    }
}

