package specs;

import io.restassured.specification.RequestSpecification;
import tests.TestBase;

import static helpers.CustomAllureListener.withCustomTemplates;
import static io.restassured.RestAssured.with;
import static io.restassured.http.ContentType.URLENC;

public class RequestSpecs {
    public static RequestSpecification baseRequestSpec = with()
            .filter(withCustomTemplates())
            .log().all()
            .header("X-Api-Key", TestBase.API_KEY) // Берем из TestBase
            .contentType(URLENC);
}
