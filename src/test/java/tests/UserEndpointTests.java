package tests;

import io.qameta.allure.*;
import models.UserAction;
import models.UserResponse;
import org.junit.jupiter.api.*;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("User Operations")
@Feature("User Lifecycle API")
@Owner("Nikita Ryazanov")
@Tag("api")
@DisplayName("Тестирование API согласно спецификации Nord Codes")
public class UserEndpointTests extends TestBase {

    @Nested
    @Story("Безопасность и авторизация")
    @DisplayName("Проверки API Key")
    class SecurityTests {
        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("Ошибка: Запрос с неверным X-Api-Key")
        void invalidApiKeyTest() {
            String token = faker.regexify("[0-9A-F]{32}");

            UserResponse response = appApi.executeActionWithCustomKey(token, UserAction.LOGIN, 401, "invalid_key");

            step("Проверка: система вернула ошибку авторизации", () -> {
                assertEquals("ERROR", response.getResult());
            });
        }
    }

    @Nested
    @Story("Позитивные сценарии")
    @DisplayName("Happy Path сценарии")
    class PositiveTests {
        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("Успешный цикл: LOGIN -> ACTION -> LOGOUT")
        void fullCycleSuccessTest() {
            String token = faker.regexify("[0-9A-F]{32}");

            step("Предусловие: Настройка моков внешнего сервиса", () -> {
                mockApi.setupAuthResponse(200);
                mockApi.setupDoActionResponse(200);
            });

            appApi.executeAction(token, UserAction.LOGIN, 200);
            appApi.executeAction(token, UserAction.ACTION, 200);
            UserResponse response = appApi.executeAction(token, UserAction.LOGOUT, 200);

            step("Проверка успешного завершения сессии", () -> {
                assertEquals("OK", response.getResult());
            });
        }
    }

    @Nested
    @Story("Обработка бизнес-логики")
    @DisplayName("Проверки состояний и хранилища")
    class BusinessLogicTests {

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Ошибка: ACTION без предварительного LOGIN")
        void actionWithoutLoginTest() {
            String token = faker.regexify("[0-9A-F]{32}");

            UserResponse response = appApi.executeAction(token, UserAction.ACTION, 403);

            step("Проверка: система запретила действие без логина", () -> {
                assertEquals("ERROR", response.getResult());
                assertTrue(response.getMessage().contains("not found"), "Ожидалось сообщение 'not found'");
            });
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Ошибка: ACTION после LOGOUT (проверка удаления токена)")
        void actionAfterLogoutTest() {
            String token = faker.regexify("[0-9A-F]{32}");

            step("Предусловие: Успешный LOGIN и последующий LOGOUT", () -> {
                mockApi.setupAuthResponse(200);
                appApi.executeAction(token, UserAction.LOGIN, 200);
                appApi.executeAction(token, UserAction.LOGOUT, 200);
            });

            UserResponse response = appApi.executeAction(token, UserAction.ACTION, 403);

            step("Проверка удаления токена из памяти", () -> {
                assertEquals("ERROR", response.getResult());
                assertTrue(response.getMessage().contains("not found"), "Ожидалось сообщение 'not found'");
            });
        }

        @Test
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Ошибка: Повторный LOGIN существующего токена")
        void duplicateLoginErrorTest() {
            String token = faker.regexify("[0-9A-F]{32}");
            mockApi.setupAuthResponse(200);

            appApi.executeAction(token, UserAction.LOGIN, 200);
            UserResponse response = appApi.executeAction(token, UserAction.LOGIN, 409);

            step("Проверка ответа на дубликат авторизации", () -> {
                assertEquals("ERROR", response.getResult());
                assertTrue(response.getMessage().contains("already exists"));
            });
        }
    }

    @Nested
    @Story("Интеграция с внешними сервисами")
    @DisplayName("Работа с Mock-сервисом")
    class MockIntegrationTests {
        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Ошибка: Внешний сервис вернул 500")
        void externalService500Test() {
            String token = faker.regexify("[0-9A-F]{32}");
            mockApi.setupAuthResponse(500);

            UserResponse response = appApi.executeAction(token, UserAction.LOGIN, 500);

            step("Проверка трансляции ошибки внешнего сервиса", () -> {
                assertEquals("ERROR", response.getResult());
            });
        }

        @Test
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Ошибка: Таймаут внешнего сервиса (11 секунд)")
        void externalServiceTimeoutTest() {
            String token = faker.regexify("[0-9A-F]{32}");
            mockApi.setupAuthResponseWithDelay(200, 11000);

            UserResponse response = appApi.executeAction(token, UserAction.LOGIN, 500);

            step("Проверка реакции приложения на таймаут", () -> {
                assertEquals("ERROR", response.getResult());
            });
        }
    }

    @Nested
    @Story("Валидация входных параметров")
    @DisplayName("Проверки формата данных")
    class ValidationTests {
        @Test
        @Severity(SeverityLevel.MINOR)
        @DisplayName("Ошибка: Токен некорректной длины (не 32 символа)")
        void shortTokenTest() {
            UserResponse response = appApi.executeAction("123", UserAction.LOGIN, 400);

            step("Проверка отклонения короткого токена", () -> {
                assertEquals("ERROR", response.getResult());
            });
        }

        @Test
        @Severity(SeverityLevel.MINOR)
        @DisplayName("Ошибка: Использование строчных букв в токене")
        void lowercaseTokenTest() {
            String token = "1234567890abcdef1234567890abcdef";
            UserResponse response = appApi.executeAction(token, UserAction.LOGIN, 400);

            step("Проверка отклонения токена в нижнем регистре", () -> {
                assertEquals("ERROR", response.getResult());
            });
        }
    }
}
