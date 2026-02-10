Item Trading Platform
A comprehensive and scalable item trading platform that integrates new product shopping and second-hand item transactions, supporting collaborative interaction among three core roles: users, merchants, and administrators. The platform is built with a front-end and back-end separation architecture to ensure high performance, security, and maintainability, while solving key issues such as cross-domain requests, data consistency, and role-based permission control.
Core Features
User Module
User registration, login, and personal information management
Secure password encryption and session management
Product browsing, fuzzy search, and filtering (by category/price/condition)
New/second-hand product purchasing, order placement, and payment
Order status tracking and historical order query (pagination supported)
Product review submission and review permission verification
Merchant Module
Merchant account authentication and management
Product release, editing, and offline operation
Order reception, processing, and status update
Sales data statistics and transaction record viewing
Response to user reviews and after-sales management
Administrator Module
Overall platform management and system configuration
Merchant account audit and permission control
Product audit (approve/reject unqualified products)
User behavior monitoring and illegal content management
Data consistency maintenance and system log viewing
System-Level Features
Cross-domain request handling and interface interaction optimization
Data integrity protection (foreign keys + unique indexes in database)
Role-based access control (RBAC) to ensure system security
High scalability for future function expansion
Tech Stack
Frontend
Framework: Vue.js (componentized layout, two-way data binding)
Routing: Vue Router (route guard for permission control)
HTTP Client: Axios (cross-domain request handling, request/response interception)
UI/Interaction: HTML5/CSS3/JavaScript (responsive design)
Backend
Core Framework: Java Servlet (request distribution, business logic processing)
ORM: MyBatis (efficient database read/write operations)
Security: BCrypt (password encryption), SessionManager (user session management)
Architecture: MVC design pattern (clear layer separation)
Database & Deployment
Database: MySQL (foreign keys, unique indexes for data integrity)
Deployment: Apache Tomcat (backend service deployment)
Quick Start
Prerequisites
JDK 8+
Node.js 14+
MySQL 5.7+/8.0+
Apache Tomcat 8.5+
Maven (for backend dependency management)
npm/yarn (for frontend dependency management)
Installation Steps
Clone the repository
bash
运行
git clone https://github.com/your-username/item-trading-platform.git
cd item-trading-platform
Database Configuration
Create a MySQL database named item_trading_platform
Import the SQL file sql/item_trading_platform.sql (located in the project root directory)
Modify the database connection configuration in backend/src/main/resources/mybatis-config.xml (update username/password/URL)
Frontend Setup
bash
运行
cd frontend
npm install  # Install dependencies
npm run build  # Build production files
Backend Setup
Import the backend project into IDE (Eclipse/IntelliJ IDEA)
Configure Maven dependencies
Deploy the backend project to Tomcat server
Run the Project
Start the MySQL service
Start the Tomcat server (backend service)
Run the frontend project (development mode):
bash
运行
cd frontend
npm run dev
Access the platform: http://localhost:8080 (adjust port according to your Tomcat/frontend configuration)

Feature Highlights
Security: BCrypt password encryption and session management prevent unauthorized access
Performance: MyBatis-based database operation optimization and pagination query reduce server load
User Experience: Fuzzy product search and responsive design improve browsing and trading efficiency
Scalability: Modular architecture supports seamless integration of new features (e.g., chat, logistics tracking)
License
This project is licensed under the MIT License - see the LICENSE file for details.
