# 📦 Secure Messaging — Веб-приложение для защищённого обмена сообщениями

## 🛡️ Описание

**Secure Messaging** — это Spring Boot приложение, позволяющее пользователям безопасно регистрироваться, входить, создавать приватные чаты и отправлять зашифрованные сообщения. Поддерживается хранение данных в MongoDB, смена языка интерфейса и тёмная/светлая темы.

## 🚀 Функциональность

- Регистрация и авторизация пользователей
- Роль `USER` по умолчанию, `ADMIN`/`TEACHER` — через базу данных
- Создание приватных чатов по email
- Отправка зашифрованных сообщений (AES)
- Темизация (светлая/тёмная)
- Локализация (RU / EN / KK)
- Валидация ошибок (повтор email, неправильный пароль и т.д.)

## 🛠️ Технологии

- Java 17 + Spring Boot 3
- Spring Security
- Spring Web + Thymeleaf
- MongoDB Atlas
- WebSockets (SockJS + STOMP)
- Bootstrap 5

## 🧩 Структура проекта

```
secure-messaging/
├── src/
│   ├── main/
│   │   ├── java/com/securemsg/
│   │   │   ├── controller/         # Контроллеры (chat, auth и др.)
│   │   │   ├── service/            # Сервисы (user, chat, message)
│   │   │   ├── model/              # Модели: User, Chat, Message
│   │   │   ├── repository/         # Интерфейсы MongoDB
│   │   │   └── config/             # Web + Security конфигурации
│   │   └── resources/
│   │       ├── templates/          # HTML-страницы (Thymeleaf)
│   │       ├── static/             # CSS, JS, изображения
│   │       ├── application.properties
│   │       └── messages_{lang}.properties
├── Dockerfile
├── .gitignore
├── pom.xml
└── README.md
```

## ⚙️ Установка локально

```bash
git clone https://github.com/ваш-профиль/secure-messaging.git
cd secure-messaging
./mvnw clean package
java -jar target/secure-messenger-1.0.0.jar
```

## 🐳 Запуск через Docker (если нужен)

```Dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t secure-msg .
docker run -p 8080:8080 secure-msg
```

## 📝 Переменные окружения

`application.properties` должен содержать:

```properties
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster.mongodb.net/?retryWrites=true&w=majority
spring.data.mongodb.database=securemsg

spring.messages.basename=messages
spring.mvc.locale=ru
spring.mvc.locale-resolver=session
```

## 🌐 Деплой на Render

1. Создайте новый Web Service: `Spring`
2. Укажите ваш GitHub репозиторий
3. В `Build Command`:
   ```bash
   ./mvnw clean package
   ```
4. В `Start Command`:
   ```bash
   java -jar target/secure-messenger-1.0.0.jar
   ```
5. Добавьте переменные окружения (Mongo URI и т.д.)
6. Выберите регион `Frankfurt` или ближе к Казахстану

---

## 🤝 Авторы

- Bagustar Tleubergen
- Astana International University
- 2025