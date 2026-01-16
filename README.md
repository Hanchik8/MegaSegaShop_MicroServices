# MegaSegaShop

## Требования
- Java 21 (preview включен: `--enable-preview`)
- Docker + Docker Compose для полного стека (PostgreSQL, Redis, Kafka)

## Переменные окружения (.env)
Минимум для Docker Compose:
- `SECURITY_JWT_SECRET` (минимум 32 символа)
- `DB_USERNAME`
- `DB_PASSWORD`

Опционально:
- `SECURITY_JWT_EXPIRATION_MINUTES` (по умолчанию 60)

Полный список и примеры: `ENVIRONMENT.md`.

## Быстрый старт (Docker Compose)
1) Создайте `.env` в корне проекта (см. `ENVIRONMENT.md`).
2) Запустите все сервисы:

```bash
docker-compose up -d --build
```

API Gateway: `http://localhost:8080`
Frontend UI: `http://localhost:8080/`

## Локальный запуск (без Docker Compose)
1) Поднимите инфраструктуру: PostgreSQL, Redis, Kafka/Zookeeper.  
   БД должны быть созданы: `auth_db`, `user_db`, `product_db`, `cart_db`, `inventory_db`, `order_db`.
2) Запустите Config Server и Discovery Server (обязательны для всех сервисов):

```bash
# PowerShell:
$env:JAVA_TOOL_OPTIONS="--enable-preview"
# Bash:
export JAVA_TOOL_OPTIONS=--enable-preview

./mvnw -pl config-server spring-boot:run
./mvnw -pl discovery-server spring-boot:run
```

3) Запустите нужные микросервисы и API Gateway:

```bash
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl auth-service spring-boot:run
./mvnw -pl user-service spring-boot:run
./mvnw -pl product-service spring-boot:run
./mvnw -pl cart-service spring-boot:run
./mvnw -pl inventory-service spring-boot:run
./mvnw -pl order-service spring-boot:run
./mvnw -pl notification-service spring-boot:run
```

## Demo сценарий (cURL)

## Предусловия
Убедитесь, что весь стек поднят через Docker Compose и доступен API Gateway:

```bash
docker-compose up -d --build
```

Базовый URL: `http://localhost:8080`

## 1) Регистрация
```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@megasega.shop",
    "password": "DemoPass123!",
    "firstName": "Demo",
    "lastName": "User",
    "phone": "+15555550123"
  }'
```

## 2) Логин (получить JWT)
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@megasega.shop",
    "password": "DemoPass123!"
  }'
```

Скопируйте `accessToken` и `userId` из ответа. Далее используйте:

```bash
TOKEN="<PASTE_ACCESS_TOKEN>"
USER_ID=<PASTE_USER_ID>
```

## 3) Создание товара (требует JWT)
```bash
curl -i -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MegaSega Wireless Pad",
    "brand": "MegaSega",
    "description": "Limited demo controller",
    "price": 49.99,
    "initialQuantity": 20,
    "category": "Accessories"
  }'
```

Скопируйте `id` из ответа:

```bash
PRODUCT_ID=<PASTE_PRODUCT_ID>
```

## 4) Добавление в корзину
```bash
curl -i -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -d '{
    "userId": '"$USER_ID"',
    "productId": '"$PRODUCT_ID"',
    "quantity": 2
  }'
```

> **Примечание:** Название товара и цена автоматически получаются из `product-service`.
> Это гарантирует корректность данных и защиту от манипуляций с ценой.

## 5) Оформление заказа
```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": '"$USER_ID"',
    "email": "demo@megasega.shop"
  }'
```

## Дополнительно
- Просмотр каталога: `GET http://localhost:8080/api/products`
- Остаток товара: `GET http://localhost:8080/api/products/{id}/stock`
- Очистка корзины: `DELETE http://localhost:8080/api/cart/{userId}`

## Local PowerShell helper (env-local.ps1)
Use this helper to load `.env` (if present) and set env vars for local runs:

```powershell
. .\env-local.ps1
```

It sets:
- `JAVA_HOME`/`PATH` and `JAVA_TOOL_OPTIONS=--enable-preview`
- `SECURITY_JWT_SECRET`
- `SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD` (from `DB_USERNAME`/`DB_PASSWORD` if present)
- `EUREKA_INSTANCE_HOSTNAME`/`EUREKA_INSTANCE_IP_ADDRESS`/`EUREKA_INSTANCE_PREFER_IP_ADDRESS` for local discovery

## Troubleshooting
### 500 on `/api/products` with `ClassCastException: LinkedHashMap -> Product`
Cause: Redis cache values were stored without type info, so cached items deserialize as `LinkedHashMap`.
Fix:
1) Flush Redis cache:

```bash
docker exec -it megasegashop-redis redis-cli FLUSHALL
```

2) Restart `product-service`.

Note: Cache serialization now includes type info (see `product-service/src/main/java/org/example/megasegashop/product/config/CacheConfig.java`).

