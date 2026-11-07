# 🛒 Cart & Order Details Microservice

## 1. Overview

The **Cart & Order Details Microservice** is a component of the Food Delivery Platform, developed using a Microservices Architecture.

Its main responsibility is to manage customers' shopping carts and the details of their orders throughout the ordering process.

It allows customers to add products to their cart, update quantities, remove items, and manage order details.

## 2. Main Responsibilities

### Cart Management
- Create and manage shopping carts.
- Add products to the cart.
- Update product quantities.
- Remove products from the cart.
- Retrieve cart contents.
- Calculate the cart total.

### Order Details Management
- Manage items associated with an order.
- Store product information and quantities.
- Calculate individual item prices and order totals.
- Retrieve order details.
- Associate order items with their corresponding orders.

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Programming language |
| Spring Boot | Backend framework |
| Spring Web | REST API development |
| Spring Data JPA | Database access |
| MySQL / PostgreSQL | Data persistence |
| Maven | Dependency management |
| Git & GitHub | Version control |

## 4. Main Components

- **Cart:** Represents a customer's shopping cart.
- **Cart Item:** Represents a product added to the cart, including its quantity and price.
- **Order Details:** Contains the products, quantities and prices associated with an order.

## 5. Project Structure

```text
cart-order-details/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       └── dto/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

## 6. API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/cart` | Create a cart |
| GET | `/api/cart/{id}` | Retrieve a cart |
| POST | `/api/cart/{id}/items` | Add an item |
| PUT | `/api/cart/{id}/items/{itemId}` | Update item quantity |
| DELETE | `/api/cart/{id}/items/{itemId}` | Remove an item |
| GET | `/api/orders/{orderId}/details` | Retrieve order details |

*Endpoints are illustrative and must be adapted to the actual implementation.*

## 7. Microservices Architecture

This microservice interacts with other components of the Food Delivery Platform.

Its main interactions include:

- **Product Catalog Service:** Retrieves product information and prices.
- **Order Management Service:** Provides cart information and order details during the ordering process.

```text
       Product Catalog Service
                 |
                 v
       Cart & Order Details
             Microservice
                 |
                 v
              Database
                 |
                 v
        Order Management Service
```

## 8. Author

Developed as part of the **Food Delivery Platform – Microservices Architecture** academic project.

