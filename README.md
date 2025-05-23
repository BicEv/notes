Notes API
Notes API — это RESTful-сервис для управления заметками с поддержкой тегов и аутентификацией пользователей на основе JWT.

Возможности:
Регистрация и вход пользователей
Создание, обновление и удаление заметок
Добавление и удаление тегов к заметкам
Поиск заметок по тегам (полное и частичное совпадение)
Получение всех тегов пользователя
Защищённый доступ к API с использованием JWT

Технологии:
Java 17+
Spring Boot
Spring Security
JWT (JSON Web Tokens)
JUnit 5 + MockMvc (интеграционные тесты)
Maven

Установка и запуск:
Клонируйте репозиторий:
git clone https://github.com/BicEv/notes.git
cd notes

Подставьте ваши PostgreSQL username/password и secretkey для jwt в app.properties


Соберите проект с помощью Maven:
./mvnw clean install

Запустите приложение:
./mvnw spring-boot:run
По умолчанию приложение будет доступно по адресу: http://localhost:8080.

Аутентификация:
Регистрация: POST /api/users/register

Вход: POST /api/users/login

После успешного входа вы получите JWT, который необходимо передавать в заголовке Authorization для доступа к защищённым эндпоинтам:
Authorization: Bearer <ваш JWT>

Примеры использования API:
Создание заметки:
POST /api/notes
Content-Type: application/json
Authorization: Bearer <ваш JWT>

{
  "text": "Моя первая заметка",
  "tags": ["работа", "важное"]
}

Поиск заметок по тегам:
GET /api/notes/tags?tags=работа,важное
Authorization: Bearer <ваш JWT>

Тестирование:
Интеграционные тесты находятся в пакете ru.bicev.notes. Для их запуска используйте:
./mvnw test

Тесты покрывают:
Регистрацию и вход пользователей
CRUD-операции с заметками
Управление тегами
Обработку ошибок и исключений

API-документация:
Проект содержит интерактивную документацию API с помощью Swagger UI, доступную через Springdoc OpenAPI.

Доступ к документации:
После запуска приложения, Swagger UI доступен по адресу:
http://localhost:8080/swagger-ui.html
или
http://localhost:8080/swagger-ui/index.html

Что можно делать:
-Смотреть список всех доступных эндпоинтов
-Отправлять запросы прямо из интерфейса Swagger
-Видеть необходимые параметры (включая JWT-токен, если требуется)
-Проверять схемы входных/выходных данных и коды ответов

Аутентификация:
Для запросов, требующих авторизации, необходимо нажать кнопку Authorize и ввести JWT токен:
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...

Проект доступен в виде образа Docker. Для удобства запуска контейнеров с приложением и базой данных, можно использовать предоставленный файл `docker-compose.yml`.
Запуск с помощью Docker Compose:
Чтобы запустить приложение, выполните следующие шаги:
1. Скачайте образ: docker pull bicev/notes-app:latest
2. Используйте `docker-compose.yml` для автоматического запуска приложения и базы данных PostgreSQL: docker-compose up

Образ доступен на Docker Hub: [bicev/notes-app:latest](https://hub.docker.com/r/bicev/notes-app)

