package api;

import io.qameta.allure.Step;
import models.UserAction;
import models.UserResponse;
import specs.ResponseSpecs;
import tests.TestBase;

import static io.restassured.RestAssured.given;
import static specs.RequestSpecs.baseRequestSpec;

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
}
