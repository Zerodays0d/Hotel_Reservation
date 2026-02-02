🏨 Hotel Reservation System

A JavaFX-based desktop application for managing hotel operations, demonstrating strong OOP and SOLID principles. The system is modular, maintainable, and extensible, with layered architecture and persistent SQLite storage.

✨ Features

👤 User Roles: Admins and Guests

📝 Customer Management: Create, read, update, delete

🛏 Room Management: Track availability and details

📅 Reservations: Book, update, and cancel rooms

💳 Payments: Record and track payments

🔐 Authentication: Secure login for guests and admins

🗂 Session Management: Track current users and permissions

💾 Persistence: SQLite database for all entities

🏗 Architecture & Design

The project follows a layered architecture:

UI Layer (JavaFX): Handles graphical interface and user interaction

Service Layer: Implements business rules and validation

DAO Layer: Abstracts database operations via interfaces for each entity

Persistence Layer (SQLite): Handles SQL execution with centralized connection management

OOP & SOLID:

Encapsulation: Domain models hide internal data

Abstraction: DAO interfaces separate contracts from SQL implementation

Polymorphism: Services interact with DAO interfaces, enabling interchangeable implementations

SOLID Principles: SRP, OCP, LSP, ISP, DIP applied through focused classes, layered design, and dependency on abstractions

⚙️ Setup & Installation

Clone the repository:

git clone https://github.com/yourusername/hotel-reservation-system.git


Open in IntelliJ IDEA, Eclipse, or NetBeans

Install Java 17+

Configure JavaFX VM options (required for GUI):

Point the module path to your JavaFX lib folder

Add the following modules: javafx.controls, javafx.fxml, javafx.graphics
Example VM args:

--module-path "C:\Users\Yeab\Downloads\Compressed\openjfx-25.0.2_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics


Run the main class:

ui.view.HotelReservationApp


Database: SQLite tables are auto-created on first run by DatabaseInitializer.

🖥 Usage

Admins manage customers, rooms, reservations, and payments

Guests can register and make bookings

Session management ensures proper access based on role

All operations are handled via services calling DAO interfaces

🤝 Contributing

Use Git and GitHub for version control

Commit meaningful, descriptive messages

Follow OOP/SOLID design conventions

Maintain existing package structure

📜 License

This project is for academic purposes. Reuse is allowed only with proper attribution.
