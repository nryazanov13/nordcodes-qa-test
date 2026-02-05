package tests;

import models.UserAction;
import models.UserResponse;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("Тестирование API для Nord Codes")
public class UserEndpointTests extends TestBase {

    @Nested
    @DisplayName("Позитивные сценарии (Happy Path)")
    class PositiveTests {
        @Test
        @DisplayName("Успешный цикл: LOGIN -> ACTION -> LOGOUT")
        void fullCycleSuccessTest() {
            String token = faker.regexify("[0-9A-F]{32}");

            mockApi.setupAuthResponse(200);
            appApi.executeAction(token, UserAction.LOGIN, 200);

            mockApi.setupDoActionResponse(200);
            appApi.executeAction(token, UserAction.ACTION, 200);

            UserResponse response = appApi.executeAction(token, UserAction.LOGOUT, 200);
            assertEquals("OK", response.getResult());
        }
    }

    @Nested
    @DisplayName("Проверки бизнес-логики")
    class BusinessLogicTests {
        @Test
        @DisplayName("Ошибка: Повторный LOGIN существующего токена")
        void duplicateLoginErrorTest() {
            String token = faker.regexify("[0-9A-F]{32}");
            mockApi.setupAuthResponse(200);

            appApi.executeAction(token, UserAction.LOGIN, 200);
            UserResponse response = appApi.executeAction(token, UserAction.LOGIN, 409);

            assertEquals("ERROR", response.getResult());
            assertTrue(response.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("Ошибка: ACTION без предварительного LOGIN")
        void actionWithoutLoginTest() {
            String token = faker.regexify("[0-9A-F]{32}");
            UserResponse response = appApi.executeAction(token, UserAction.ACTION, 403);

            assertEquals("ERROR", response.getResult());
            assertTrue(response.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Ошибка: LOGOUT несуществующего токена")
        void logoutWithoutLoginTest() {
            String token = faker.regexify("[0-9A-F]{32}");
            UserResponse response = appApi.executeAction(token, UserAction.LOGOUT, 403);

            assertEquals("ERROR", response.getResult());
        }
    }

    @Nested
    @DisplayName("Интеграция с внешним сервисом (Mocks)")
    class MockIntegrationTests {
        @Test
        @DisplayName("Ошибка: Внешний сервис вернул 500 при авторизации")
        void externalService500Test() {
            String token = faker.regexify("[0-9A-F]{32}");
            mockApi.setupAuthResponse(500);

            UserResponse response = appApi.executeAction(token, UserAction.LOGIN, 500);
            assertEquals("ERROR", response.getResult());
        }

        @Test
        @DisplayName("Ошибка: Таймаут внешнего сервиса (задержка 11с)")
        void externalServiceTimeoutTest() {
            String token = faker.regexify("[0-9A-F]{32}");
            mockApi.setupAuthResponseWithDelay(200, 11000);

            UserResponse response = appApi.executeAction(token, UserAction.LOGIN, 500);
            assertEquals("ERROR", response.getResult());
        }
    }

    @Nested
    @DisplayName("Валидация и Безопасность")
    class SecurityValidationTests {
        @Test
        @DisplayName("Ошибка: Неверный формат токена (слишком короткий)")
        void shortTokenTest() {
            appApi.executeAction("123", UserAction.LOGIN, 400);
        }

        @Test
        @DisplayName("Ошибка: Использование строчных букв в токене")
        void lowercaseTokenTest() {
            String token = "1234567890abcdef1234567890abcdef"; // Должны быть заглавные
            appApi.executeAction(token, UserAction.LOGIN, 400);
        }

        @Test
        @DisplayName("Ошибка: Недопустимые символы в токене (буква Z)")
        void invalidCharTokenTest() {
            String token = "ZZZZZ67890ABCDEF1234567890ABCDEF";
            appApi.executeAction(token, UserAction.LOGIN, 400);
        }
    }
}
