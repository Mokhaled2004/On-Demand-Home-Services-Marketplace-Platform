# Project Structure - Microservices Marketplace

## 📁 Eureka Server (Detailed)

```
eureka-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/marketplace/eureka/
│   │   │       └── EurekaServerApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/marketplace/eureka/
│               └── EurekaServerApplicationTests.java
├── pom.xml
├── README.md
└── .gitignore
```

### Key Files:

**EurekaServerApplication.java:**
```
- @EnableEurekaServer annotation
- Main Spring Boot application class
- Runs on port 8761
```

**application.yml:**
```
- Server port: 8761
- Eureka client config (register-with-eureka: false)
- Self-preservation settings
```

---

## 📁 User Service (Basic Skeleton)

```
user-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/marketplace/user/
│   │   │       ├── UserServiceApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── entity/
│   │   │       └── repository/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

---

## 📁 Service Catalog Service (Basic Skeleton)

```
service-catalog-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/marketplace/catalog/
│   │   │       ├── ServiceCatalogApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── entity/
│   │   │       └── repository/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

---

## 📁 Booking Service (Basic Skeleton - EJB)

```
booking-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/marketplace/booking/
│   │   │       ├── BookingServiceApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── ejb/
│   │   │       │   ├── BookingStatelessBean.java
│   │   │       │   └── BookingMessageDrivenBean.java
│   │   │       ├── entity/
│   │   │       └── repository/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

---

## 📁 Notification Service (Basic Skeleton)

```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/marketplace/notification/
│   │   │       ├── NotificationServiceApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── consumer/
│   │   │       ├── entity/
│   │   │       └── repository/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

---

## 📁 Admin Service (Basic Skeleton)

```
admin-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/marketplace/admin/
│   │   │       ├── AdminServiceApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── entity/
│   │   │       └── repository/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

---

## 📁 Root Project Structure

```
marketplace-platform/
├── eureka-server/                    ⭐ FOCUS HERE NOW
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── user-service/                     (skeleton only)
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── service-catalog-service/          (skeleton only)
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── booking-service/                  (skeleton only)
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── notification-service/             (skeleton only)
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── admin-service/                    (skeleton only)
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── docker-compose.yml                (for RabbitMQ + PostgreSQL)
├── pom.xml                           (parent pom)
├── README.md
└── .gitignore
```

---

## 🎯 Current Focus: Eureka Server Only

**What to create now:**
1. eureka-server/ folder
2. src/main/java/com/marketplace/eureka/EurekaServerApplication.java
3. src/main/resources/application.yml
4. pom.xml

**Other services:** Just create empty folder structure with pom.xml (no implementation yet)

---

## 📝 Parent pom.xml (Optional but Recommended)

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.marketplace</groupId>
    <artifactId>marketplace-platform</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>eureka-server</module>
        <module>user-service</module>
        <module>service-catalog-service</module>
        <module>booking-service</module>
        <module>notification-service</module>
        <module>admin-service</module>
    </modules>
</project>
```

This allows running all services from root: `mvn clean install`
