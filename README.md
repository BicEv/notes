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
