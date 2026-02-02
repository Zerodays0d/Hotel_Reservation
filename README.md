Hotel Reservation System

A JavaFX-based desktop application for managing hotel operations, built with Java SE and SQLite. The system demonstrates strong object-oriented programming (OOP) principles and adherence to SOLID design principles, providing a maintainable, modular, and extensible architecture.

Features

Supports multiple user roles, including administrators and guests

Customer management with create, read, update, and delete operations

Room management and tracking of availability

Reservation management: create, update, cancel, and validate bookings

Payment recording and tracking for reservations

Secure login and authentication for both guests and administrators

Session management for tracking currently logged-in users

Persistent storage using SQLite

Architecture & Design

The system is organized into multiple layers:

UI Layer: JavaFX handles all user interactions and interface rendering

Service Layer: Implements business logic and validation for operations like reservations, payments, and authentication

DAO Layer: Abstracts database operations through interfaces for each entity (CustomerDAO, RoomDAO, ReservationDAO, PaymentDAO, UserDAO)

Persistence Layer: SQLite implementations of the DAO interfaces (dao.sqlite.*) handle SQL execution, using a centralized connection manager

The project applies OOP principles by encapsulating data in domain models, abstracting persistence through DAO interfaces, and separating responsibilities across layers. SOLID principles are applied by keeping classes focused on single responsibilities, depending on abstractions rather than concrete implementations, allowing extension without modification, and exposing only relevant operations.

Setup & Installation

Clone the repository to your local machine.

Open the project in IntelliJ IDEA, Eclipse, or NetBeans.

Ensure Java 17 or higher is installed.

Configure JavaFX to run the GUI. You need to set the VM options to include the JavaFX library path and modules. For example, in IntelliJ IDEA, go to Run > Edit Configurations > VM options and add the following:

The module path should point to the lib folder of your JavaFX SDK

The modules to add are javafx.controls, javafx.fxml, and javafx.graphics
This ensures the JavaFX runtime is loaded correctly.

Run the main class: ui.view.HotelReservationApp.

The SQLite database will be automatically initialized on the first run, creating all necessary tables.

Usage

Administrators can log in to manage customers, rooms, reservations, and payments.

Guests can register and make reservations.

Session state ensures that each user only accesses permitted features.

CRUD operations and business rules are handled through the service layer.

Contributing

Use Git and GitHub for version control.

Commit meaningful changes with descriptive messages.

Follow the existing package structure and maintain OOP/SOLID conventions.

License

This project is for academic purposes. Reuse is allowed only with proper attribution.
