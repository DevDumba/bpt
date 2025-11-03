# 💳 Basic Payment Transfer Service (BPT)

A Spring Boot microservice designed to handle **secure fund transfers** between user accounts within a banking system.  
The project demonstrates **transactional integrity**, **Kafka event-driven communication**, and **Dockerized infrastructure** for distributed systems.

---

## 🧩 Features

- ✅ Transfer funds between two accounts (atomic transactions)
- ✅ Kafka producer/consumer event handling (`transfer-events` topic)
- ✅ H2 in-memory database for testing
- ✅ RESTful API with OpenAPI (Swagger) documentation
- ✅ Dockerized Kafka and Zookeeper setup
- ✅ Logging and error handling

---

## ⚙️ Technologies Used

| Layer | Technology |
|-------|-------------|
| Backend | Java 17, Spring Boot |
| Messaging | Apache Kafka, Zookeeper |
| Database | H2 (In-Memory) |
| Build Tool | Maven |
| Containerization | Docker |
| Documentation | Swagger / OpenAPI |
| Logging | SLF4J, Lombok |

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/DevDumba/bpt.git
cd bpt
```

### 2. Docker and Kafka
```bash
docker network create kafka-net

docker run -d --name zookeeper --network kafka-net -p 2181:2181 confluentinc/cp-zookeeper:latest

docker run -d --name kafka --network kafka-net -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest

docker ps
```

## 🌐 API Endpoints

| Method | Endpoint         | Description                                   |
| ------ | ---------------- | --------------------------------------------- |
| POST   | `/api/transfers` | Executes a fund transfer between two accounts |

### 🧾 Example Request
```json
{
  "sourceAccount": "205-0000001234567-68",
  "destinationAccount": "205-0000007654321-68",
  "amount": 500.00
}
```
### ✅ Example Successful Response
```json
{
  "transferId": 10,
  "sourceAccount": "205-0000001234567-68",
  "destinationAccount": "205-0000007654321-68",
  "amount": 200.00,
  "status": "SUCCESS",
  "timestamp": "2025-11-03T08:57:47.2455865"
}
```

### ❌ Example Error Response
```json
{
  "timestamp": "2025-11-03T08:58:16.8674414",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient funds on source account"
}
```


## 🧩 Architecture Diagram

         +--------------------+
         |  TransferController|
         |  (REST Endpoint)   |
         +---------+----------+
                   |
                   v
         +---------+----------+
         |  TransferService   |
         |  (Business Logic)  |
         +---------+----------+
                   |
                   v
         +---------+----------+
         |  TransferRepository|
         |  AccountRepository |
         |  (Database Layer)  |
         +---------+----------+
                   |
                   v
         +---------+----------+
         |  Kafka Producer    |
         |  send TransferEvent|
         +---------+----------+
                   |
                   v
         +---------+----------+
         |  Kafka Broker      |
         |  (transfer-events) |
         +---------+----------+
                   |
                   v
         +---------+----------+
         |  Kafka Consumer    |
         |  log event details |
         +--------------------+

## 📜 Swagger API Documentation
Once the app is running, open in your browser:
👉 http://localhost:8080/swagger-ui.html

## 🧩 Project Structure

```bash
src/main/java/com/example/bpt
│
├── controller/
│   └── TransferController.java           # REST API endpoints
│
├── dto/
│   ├── TransferRequest.java              # DTO for transfer input
│   └── TransferResponse.java             # DTO for transfer output
│
├── event/
│   └── TransferCompletedEvent.java       # Kafka event payload
│
├── exception/
│   ├── GlobalExceptionHandler.java       # Centralized exception handling
│   └── ResourceNotFoundException.java    # Custom not-found exception
│
├── kafka/
│   ├── TransferEventProducer.java        # Kafka producer for transfer events
│   └── TransferEventConsumer.java        # Kafka consumer for transfer events
│
├── model/
│   ├── Account.java                      # Account entity
│   ├── Transfer.java                     # Transfer entity
│   └── User.java                         # User entity
│
├── repository/
│   ├── AccountRepository.java            # JPA repository for accounts
│   ├── TransferRepository.java           # JPA repository for transfers
│   └── UserRepository.java               # JPA repository for users
│
├── service/
│   ├── TransferService.java              # Service interface
│   └── impl/
│       └── TransferServiceImpl.java      # Business logic implementation
│
├── util/
│   ├── AccountUtils.java                 # Helper methods for account operations
│   └── ServletInitializer.java           # Application servlet initializer
│
├── BptApplication.java                   # Spring Boot main entry point
```

## 🗄️ Database Setup

### 1️⃣ Create Database Schema

This project uses **MySQL** as the primary relational database.

Run the following SQL script to set up the database tables and initial data:

```sql
CREATE DATABASE bpt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bpt;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(25) NOT NULL UNIQUE,
    balance DECIMAL(18,2) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transfer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_account_id BIGINT NOT NULL,
    destination_account_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    source_old_balance DECIMAL(18,2) NOT NULL,
    source_new_balance DECIMAL(18,2) NOT NULL,
    destination_old_balance DECIMAL(18,2) NOT NULL,
    destination_new_balance DECIMAL(18,2) NOT NULL,
    timestamp DATETIME NOT NULL,
    performed_by BIGINT NOT NULL,
    FOREIGN KEY (source_account_id) REFERENCES account(id),
    FOREIGN KEY (destination_account_id) REFERENCES account(id),
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

INSERT INTO users (username, email)
VALUES ('marko', 'marko@example.com'),
       ('jovan', 'jovan@example.com');

INSERT INTO account (account_number, balance, user_id)
VALUES ('205-0000001234567-68', 5000.00, 1),
       ('205-0000007654321-68', 2000.00, 2);

CREATE USER 'bpt_user'@'%' IDENTIFIED BY 'Bpt#2025';
GRANT ALL PRIVILEGES ON bpt.* TO 'bpt_user'@'%';
FLUSH PRIVILEGES;
```

### ⚙️ Database application properties
```bash
spring.datasource.url=jdbc:mysql://localhost:3306/bpt
spring.datasource.username=bpt_user
spring.datasource.password=Bpt#2025
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```
