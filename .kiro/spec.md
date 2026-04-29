# On-Demand Home Services Marketplace - Implementation Spec

## Project Overview
<cite index="1-1,1-2">Build a distributed on-demand home services marketplace where customers can book skilled professionals (plumbers, carpenters, electricians, cleaners, painters, etc.) for service requests</cite>.

## Architecture

### Microservices Design
<cite index="1-40,1-41,1-42,1-43">The system should be designed to follow the microservice architectural style. Each service should be implemented as its own project with its own codebase and its own DB. Services should request information from other services through REST calls rather than accessing other databases directly</cite>.

**Services to Implement:**
1. **User Service** - Handle user registration, authentication, and user management
2. **Service Catalog Service** - Manage service categories and service offers
3. **Booking Service** - Handle booking logic and wallet management (EJB-based)
4. **Notification Service** - Send async notifications via RabbitMQ
5. **Admin Service** - Admin operations and reporting

### Technology Stack
- **Framework**: Java with EJBs (one service), Spring or plain Java for others
- **Message Queue**: RabbitMQ for async communication
- **Database**: Separate DB per service
- **API**: REST endpoints for all operations

## Functional Requirements

### Admin Capabilities
<cite index="1-9,1-10,1-11,1-12">Admins should be able to: view all registered users, view all transaction history for all users (booking records), and add a new service category</cite>.

### Service Provider Capabilities
<cite index="1-12,1-14,1-15,1-16,1-17,1-19,1-20,1-21">Service professionals should be able to: register with profession type in addition to username and password, login to the system, create service offer with price and available date, view all active service offers and their details, update a service offer pricing and availability, view completed services with their info including customer names, and receive and view booking information</cite>.

### Customer Capabilities
<cite index="1-23,1-24,1-25,1-26,1-27,1-29,1-30,1-31">Customers should be able to: register as a new customer with username, password, and balance, set initial wallet balance during registration, add funds to wallet at any time, view current wallet balance, browse available services by category, book a professional service, receive booking confirmation and status updates, and view booking history</cite>.

## RabbitMQ Requirements

<cite index="1-32,1-33">When a booking is made, the system verifies that the customer wallet has sufficient balance. If available and balance confirmed: deduct amount from wallet → complete booking with all information → send confirmation to the customer and the service provider. If unavailable or insufficient balance: reject booking → rollback all actions → return deducted amount to wallet and send a notification to the customer. All confirmations are sent asynchronously and all notifications are delivered through REST API endpoints that return notification data in JSON format</cite>.

<cite index="1-38,1-39">Admins should be notified in case of payment failure using RabbitMQ direct exchange feature</cite>.

## EJB Requirements

<cite index="1-45,1-46">One of the services must be developed using EJBs while applying two EJB types. You are required to use any two of these 4 different bean types: Stateless, Stateful, Singleton, or Message Driven</cite>.

## Implementation Tasks

### Phase 1: Project Setup
- [ ] Create microservice project structure
- [ ] Set up database schemas for each service
- [ ] Configure RabbitMQ message exchanges and queues
- [ ] Set up REST API frameworks

### Phase 2: Core Services
- [ ] Implement User Service (registration, authentication)
- [ ] Implement Service Catalog Service (categories, offers)
- [ ] Implement Booking Service with EJBs
- [ ] Implement Notification Service

### Phase 3: Integration
- [ ] Connect services via REST APIs
- [ ] Implement RabbitMQ message producers/consumers
- [ ] Implement wallet and payment logic
- [ ] Implement booking confirmation workflow

### Phase 4: Testing & Delivery
- [ ] Create Postman collection for all endpoints
- [ ] Test all functional requirements
- [ ] Document setup and deployment steps

## Deliverables
<cite index="1-46">Source code for the developed application and Postman collection to test all the endpoints of all the function requirements</cite>.

<cite index="1-62">Deadline for the submission is May 8th, 2026</cite>.
