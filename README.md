# High-Concurrency Flash Sale Backend System

## 🧩 Project Overview

This project implements a high-performance flash sale backend system. It solves key issues in flash sale scenarios such as **concurrent inventory deduction**, **idempotent order processing**, and **asynchronous message handling**.

It uses:
- **Redis** for inventory caching and atomic deduction via Lua scripts
- **Kafka** for asynchronous order processing
- **MySQL** for persistent order storage

---

## 🛠 Tech Stack

- **Spring Boot 3.5.3** - Lightweight Java framework
- **Redis** - Inventory preloading and atomic operations
- **Kafka** - Asynchronous message queue
- **MySQL** - Order persistence
- **Spring Data JPA** - ORM for database operations
- **Lombok** - Reduce boilerplate code
- **Swagger** - API documentation (optional)

---

## 🗂 Module Structure

| Module                  | Description                                 |
|-------------------------|---------------------------------------------|
| `SeckillController`     | REST API for flash sale                     |
| `RedisService`          | Inventory caching and Redis interactions    |
| `KafkaSender`           | Produces Kafka messages for orders          |
| `OrderService`          | Inventory deduction, order creation, TX mgmt|
| `OrderConsumer`         | Consumes Kafka messages and processes orders|
| `RateLimiterService`    | Redis-based request rate limiting           |
| `GlobalExceptionHandler`| Global exception handling                   |

---

## 🎯 Key Design Concepts

### ✅ Inventory Preload & Atomic Deduction

- Load inventory from DB to Redis on startup
- Use Lua script for atomic deduction to avoid overselling

### ✅ Idempotency

- Unique constraint on (user_id, product_id) in DB to prevent duplicate orders
- Redis stores request markers for fast duplicate-check
- Kafka ensures at-least-once delivery; consumer logic is idempotent

### ✅ Asynchronous Order Processing

- `/seckill/buy` pushes order request to Kafka
- Consumer processes order asynchronously (deduction + DB insert)
- Result stored back in Redis for frontend query

### ✅ Request Rate Limiting

- Redis token-bucket algorithm to limit QPS per IP
- Avoids malicious flooding

### ✅ Transaction Management

- Inventory deduction + order creation in a single DB transaction
- Kafka used to decouple write pressure from user-facing interface

---

## ⚙️ Runtime Requirements

- JDK 17
- Redis 6+
- Kafka 2.8+
- MySQL 8+

Config files:
- `application.yml` (or `application.properties`)  
  Includes Redis, Kafka, DB configs, and rate limit settings.

---

## 🚀 Startup Process

1. Start application → preload inventory from DB to Redis
2. Clients call `/seckill/buy` to attempt purchase
3. System checks rate limit + duplicate purchase
4. Push request to Kafka asynchronously
5. Kafka consumer deducts inventory + saves order
6. Result written to Redis for status query

---

## 📊 Stress Test

- Tool: Apache JMeter
- Scenario: 500 users concurrently purchasing the same item
- Result targets:
    - QPS > 500
    - Failure rate < 5%
    - No duplicate orders
    - No overselling

---

## 🧱 Logging & Exception Handling

- Centralized global exception handler
- Meaningful business logs (in Chinese) for tracking

---

## 🔮 Future Improvements

- JVM tuning for better GC and throughput
- Optimize rate limiting algorithm
- Kafka producer batching and retry policy
- Optional: Distributed transaction support
- Build admin dashboard & system monitor

---

## 📬 Contact

Maintainer: Tingchang Deng  
Email: dengtingchang@gmail.com  
GitHub: [https://github.com/xmenmagneto](https://github.com/xmenmagneto)