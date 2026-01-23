# MegaSegaShop

Микросервисное e-commerce приложение на Spring Boot 3 и Spring Cloud.

## Архитектура

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   API Gateway   │────▶│ Discovery Server│◀────│  Config Server  │
│     :8080       │     │     :8761       │     │     :8888       │
└────────┬────────┘     └─────────────────┘     └─────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│                        Микросервисы                            │
├──────────┬──────────┬──────────┬──────────┬──────────┬────────┤
│   Auth   │   User   │ Product  │   Cart   │ Inventory│ Order  │
│  :8081   │  :8082   │  :8083   │  :8084   │  :8085   │ :8086  │
└──────────┴──────────┴──────────┴──────────┴──────────┴────────┘
         │                 │                       │
         ▼                 ▼                       ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────┐
│   PostgreSQL    │ │      Redis      │ │  Kafka + Notification   │
│     :5432       │ │     :6379       │ │   :9092      :8087      │
└─────────────────┘ └─────────────────┘ └─────────────────────────┘
```

## Технологии

- **Java 21** с preview features
- **Spring Boot 3.3** / Spring Cloud 2023.0
- **PostgreSQL 16** — основная БД (по базе на сервис)
- **Redis 7** — кэширование (product-service, cart-service)
- **Kafka** — асинхронные события (заказы, товары)
- **Resilience4j** — Circuit Breaker для Feign-клиентов
- **Micrometer + Zipkin** — распределённая трассировка

## Быстрый старт

### Docker Compose (рекомендуется)

```bash
# Создайте .env файл (см. ENVIRONMENT.md)
docker-compose up -d --build
```

### Доступные URL

| Сервис | URL |
|--------|-----|
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Zipkin (трассировка) | http://localhost:9411 |
| Swagger UI (auth) | http://localhost:8081/swagger-ui.html |
| Swagger UI (products) | http://localhost:8083/swagger-ui.html |

## Переменные окружения

Обязательные для Docker Compose:

```env
SECURITY_JWT_SECRET=your-secret-min-32-chars
DB_USERNAME=megasega
DB_PASSWORD=megasega
```

Подробнее: `ENVIRONMENT.md`

## API Demo

### 1. Регистрация и логин

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"Password123","firstName":"Demo","lastName":"User"}'

# Логин → получаем accessToken
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"Password123"}'
```

### 2. Работа с товарами

```bash
# Каталог (публичный)
curl http://localhost:8080/api/products

# Создание товара (требует ADMIN роль)
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Product","brand":"Brand","price":99.99,"category":"Category","initialQuantity":10}'
```

### 3. Корзина и заказ

```bash
# Добавить в корзину
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":2}'

# Оформить заказ
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"email":"demo@test.com"}'
```

## Локальный запуск

```bash
# 1. Поднять инфраструктуру
docker-compose up -d postgres redis kafka zookeeper zipkin

# 2. Установить preview mode
export JAVA_TOOL_OPTIONS=--enable-preview  # Linux/Mac
$env:JAVA_TOOL_OPTIONS="--enable-preview"  # PowerShell

# 3. Запустить сервисы (в порядке зависимостей)
./mvnw -pl config-server spring-boot:run
./mvnw -pl discovery-server spring-boot:run
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl auth-service,product-service,cart-service,order-service spring-boot:run
```

## Структура проекта

```
MegaSegaShop/
├── .shared/                 # Общие модули
│   ├── shared-security/     # JWT утилиты
│   ├── shared-web/          # GlobalExceptionHandler, @AdminOnly
│   └── shared-dto/          # Общие DTO
├── config-server/           # Централизованная конфигурация
├── discovery-server/        # Eureka Server
├── api-gateway/             # Spring Cloud Gateway + JWT фильтр
├── auth-service/            # Регистрация, логин, JWT
├── user-service/            # Профили пользователей
├── product-service/         # Каталог товаров + Redis кэш
├── cart-service/            # Корзина покупок
├── inventory-service/       # Управление остатками (Kafka consumer)
├── order-service/           # Заказы (Saga pattern)
├── notification-service/    # Уведомления (Kafka consumer)
└── payment-service/         # Заглушка для оплаты
```

## Тестирование

```bash
# Все тесты
./mvnw test

# Конкретный модуль
./mvnw -pl auth-service test
```

## Документация

- `ENVIRONMENT.md` — переменные окружения
- `AGENTS.md` — guidelines для разработки
- Swagger UI доступен на каждом сервисе: `/swagger-ui.html`
