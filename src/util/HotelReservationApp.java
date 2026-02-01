package ui.view;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.*;
import service.*;
import util.DatabaseInitializer;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Hotel Reservation System - Main JavaFX Application.
 * Layered MVC: UI delegates to Controllers/Services; no SQL in UI.
 */
public class HotelReservationApp extends Application {

    private static final int SIDE_PANEL_WIDTH = 220;
    private static final String FIELD_STYLE = "-fx-background-color: rgba(255, 255, 255, 0.08); " +
            "-fx-background-radius: 12; -fx-text-fill: #e8e8e8; -fx-prompt-text-fill: #606060; " +
            "-fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 12; -fx-padding: 0 16 0 16;";
    private static final String DIALOG_FIELD = "-fx-background-color: #f5f5f5; -fx-background-radius: 8; " +
            "-fx-text-fill: #1a1a1a; -fx-prompt-text-fill: #666; -fx-font-size: 14px;";
    private static final String BTN_STYLE = "-fx-background-color: #e94560; -fx-background-radius: 12; " +
            "-fx-text-fill: white; -fx-cursor: hand;";
    private static final String BTN_HOVER = "-fx-background-color: #ff6b6b; -fx-background-radius: 12; " +
            "-fx-text-fill: white; -fx-cursor: hand;";
    private static final String SIDE_BTN = "-fx-background-color: transparent; -fx-text-fill: #c0c0c0; " +
            "-fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand; -fx-padding: 14 20;";
    private static final String SIDE_BTN_SEL = "-fx-background-color: rgba(233, 69, 96, 0.2); -fx-text-fill: #e94560; " +
            "-fx-font-size: 14px; -fx-alignment: center-left; -fx-cursor: hand; -fx-padding: 14 20;";

    private BorderPane mainLayout;
    private StackPane contentArea;
    private Button dashboardBtn, roomBtn, customerBtn, reservationBtn, paymentBtn;
    private Button guestHomeBtn, guestBookBtn, guestReservationsBtn, guestPaymentsBtn, guestProfileBtn;
    private VBox landingView;

    private TextField styleField(TextField f) {
        f.setFont(Font.font("Segoe UI", 14));
        f.setPrefHeight(48);
        f.setStyle(FIELD_STYLE);
        f.setPrefWidth(320);
        return f;
    }

    private PasswordField stylePassword(PasswordField f) {
        f.setFont(Font.font("Segoe UI", 14));
        f.setPrefHeight(48);
        f.setStyle(FIELD_STYLE);
        f.setPrefWidth(320);
        return f;
    }

    private void styleDialogField(javafx.scene.Node c) {
        if (c instanceof TextField) {
            ((TextField) c).setPrefWidth(280);
            ((TextField) c).setStyle(DIALOG_FIELD);
            ((TextField) c).setFont(Font.font("Segoe UI", 14));
        } else if (c instanceof PasswordField) {
            ((PasswordField) c).setPrefWidth(280);
            ((PasswordField) c).setStyle(DIALOG_FIELD);
            ((PasswordField) c).setFont(Font.font("Segoe UI", 14));
        } else if (c instanceof ComboBox) {
            ((ComboBox<?>) c).setPrefWidth(280);
            ((ComboBox<?>) c).setPrefHeight(40);
            ((ComboBox<?>) c).setStyle(DIALOG_FIELD);
        } else if (c instanceof TextArea) {
            ((TextArea) c).setPrefWidth(280);
            ((TextArea) c).setStyle(DIALOG_FIELD);
        } else if (c instanceof DatePicker) {
            ((DatePicker) c).setPrefWidth(280);
            ((DatePicker) c).setStyle(DIALOG_FIELD);
        } else if (c instanceof Spinner) {
            ((Spinner<?>) c).setPrefWidth(280);
            ((Spinner<?>) c).setStyle(DIALOG_FIELD);
        }
    }

    private void styleDialogLabel(Label l) {
        l.setStyle("-fx-text-fill: #1a1a1a;");
    }

    private void addDialogRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label lbl = new Label(labelText);
        styleDialogLabel(lbl);
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }

    private Button styleButton(Button b) {
        b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        b.setPrefHeight(48);
        b.setPrefWidth(320);
        b.setStyle(BTN_STYLE);
        b.setOnMouseEntered(e -> b.setStyle(BTN_HOVER));
        b.setOnMouseExited(e -> b.setStyle(BTN_STYLE));
        return b;
    }

    private void selectSideButton(Button selected) {
        if (dashboardBtn != null) dashboardBtn.setStyle(SIDE_BTN);
        if (roomBtn != null) roomBtn.setStyle(SIDE_BTN);
        if (customerBtn != null) customerBtn.setStyle(SIDE_BTN);
        if (reservationBtn != null) reservationBtn.setStyle(SIDE_BTN);
        if (paymentBtn != null) paymentBtn.setStyle(SIDE_BTN);
        if (selected != null) selected.setStyle(SIDE_BTN_SEL);
    }

    private void selectGuestSideButton(Button selected) {
        if (guestHomeBtn != null) guestHomeBtn.setStyle(SIDE_BTN);
        if (guestBookBtn != null) guestBookBtn.setStyle(SIDE_BTN);
        if (guestReservationsBtn != null) guestReservationsBtn.setStyle(SIDE_BTN);
        if (guestPaymentsBtn != null) guestPaymentsBtn.setStyle(SIDE_BTN);
        if (guestProfileBtn != null) guestProfileBtn.setStyle(SIDE_BTN);
        if (selected != null) selected.setStyle(SIDE_BTN_SEL);
    }

    private void switchToAdminDashboard() {
        mainLayout.setLeft(buildAdminSidePanel());
        selectSideButton(dashboardBtn);
        showDashboard();
        mainLayout.setCenter(contentArea);
    }

    private void switchToGuestDashboard() {
        mainLayout.setLeft(buildGuestSidePanel());
        selectGuestSideButton(guestHomeBtn);
        showGuestHome();
        mainLayout.setCenter(contentArea);
    }

    private void showMainWithSidePanel() {
        if (SessionManager.isAdmin()) mainLayout.setLeft(buildAdminSidePanel());
        else mainLayout.setLeft(buildGuestSidePanel());
    }

    private VBox buildGuestSidePanel() {
        VBox side = new VBox(4);
        side.setPrefWidth(SIDE_PANEL_WIDTH);
        side.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
        side.setPadding(new Insets(24, 0, 24, 0));

        Label title = new Label("Guest");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#e8e8e8"));
        title.setPadding(new Insets(0, 0, 24, 20));
        side.getChildren().add(title);

        guestHomeBtn = new Button("Home");
        guestHomeBtn.setMaxWidth(Double.MAX_VALUE);
        guestHomeBtn.setStyle(SIDE_BTN_SEL);
        guestHomeBtn.setOnAction(e -> { selectGuestSideButton(guestHomeBtn); showGuestHome(); });

        guestBookBtn = new Button("Book a Room");
        guestBookBtn.setMaxWidth(Double.MAX_VALUE);
        guestBookBtn.setStyle(SIDE_BTN);
        guestBookBtn.setOnAction(e -> { selectGuestSideButton(guestBookBtn); showGuestBookRoom(); });

        guestReservationsBtn = new Button("My Reservations");
        guestReservationsBtn.setMaxWidth(Double.MAX_VALUE);
        guestReservationsBtn.setStyle(SIDE_BTN);
        guestReservationsBtn.setOnAction(e -> { selectGuestSideButton(guestReservationsBtn); showGuestReservations(); });

        guestPaymentsBtn = new Button("My Payments");
        guestPaymentsBtn.setMaxWidth(Double.MAX_VALUE);
        guestPaymentsBtn.setStyle(SIDE_BTN);
        guestPaymentsBtn.setOnAction(e -> { selectGuestSideButton(guestPaymentsBtn); showGuestPayments(); });

        guestProfileBtn = new Button("Profile");
        guestProfileBtn.setMaxWidth(Double.MAX_VALUE);
        guestProfileBtn.setStyle(SIDE_BTN);
        guestProfileBtn.setOnAction(e -> { selectGuestSideButton(guestProfileBtn); showGuestProfile(); });

        side.getChildren().addAll(guestHomeBtn, guestBookBtn, guestReservationsBtn, guestPaymentsBtn, guestProfileBtn);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        side.getChildren().add(spacer);

        Button logoutBtn = new Button("Log out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle(SIDE_BTN);
        logoutBtn.setOnAction(e -> {
            SessionManager.logout();
            AuthService.getInstance().logout();
            mainLayout.setLeft(null);
            mainLayout.setCenter(landingView);
        });
        side.getChildren().add(logoutBtn);
        return side;
    }

    private VBox buildAdminSidePanel() {
        VBox side = new VBox(4);
        side.setPrefWidth(SIDE_PANEL_WIDTH);
        side.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
        side.setPadding(new Insets(24, 0, 24, 0));

        Label title = new Label("Hotel");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#e8e8e8"));
        title.setPadding(new Insets(0, 0, 24, 20));
        side.getChildren().add(title);

        dashboardBtn = new Button("Dashboard");
        dashboardBtn.setMaxWidth(Double.MAX_VALUE);
        dashboardBtn.setStyle(SIDE_BTN_SEL);
        dashboardBtn.setOnAction(e -> {
            selectSideButton(dashboardBtn);
            showDashboard();
        });

        roomBtn = new Button("Room Management");
        roomBtn.setMaxWidth(Double.MAX_VALUE);
        roomBtn.setStyle(SIDE_BTN);
        roomBtn.setOnAction(e -> {
            selectSideButton(roomBtn);
            showRoomManagement();
        });

        customerBtn = new Button("Customer Management");
        customerBtn.setMaxWidth(Double.MAX_VALUE);
        customerBtn.setStyle(SIDE_BTN);
        customerBtn.setOnAction(e -> {
            selectSideButton(customerBtn);
            showCustomerManagement();
        });

        reservationBtn = new Button("Reservation Management");
        reservationBtn.setMaxWidth(Double.MAX_VALUE);
        reservationBtn.setStyle(SIDE_BTN);
        reservationBtn.setOnAction(e -> {
            selectSideButton(reservationBtn);
            showReservationManagement();
        });

        paymentBtn = new Button("Payments");
        paymentBtn.setMaxWidth(Double.MAX_VALUE);
        paymentBtn.setStyle(SIDE_BTN);
        paymentBtn.setOnAction(e -> {
            selectSideButton(paymentBtn);
            showPaymentManagement();
        });

        side.getChildren().addAll(dashboardBtn, roomBtn, customerBtn, reservationBtn, paymentBtn);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        side.getChildren().add(spacer);

        Button logoutBtn = new Button("Log out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle(SIDE_BTN);
        logoutBtn.setOnAction(e -> {
            SessionManager.logout();
            AuthService.getInstance().logout();
            mainLayout.setLeft(null);
            mainLayout.setCenter(landingView);
        });
        side.getChildren().add(logoutBtn);

        return side;
    }

    private void showDashboard() {
        contentArea.getChildren().clear();
        VBox main = new VBox(20);
        main.setPadding(new Insets(24));

        Label heading = new Label("Dashboard");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));

        Label welcome = new Label("Welcome, " + SessionManager.getCurrentDisplayName() + "!");
        welcome.setFont(Font.font("Segoe UI", 18));
        welcome.setTextFill(Color.web("#c0c0c0"));

        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.06); -fx-background-radius: 12;");

        int roomCount = RoomService.getInstance().findAll().size();
        int customerCount = CustomerService.getInstance().findAll().size();
        int reservationCount = ReservationService.getInstance().findAll().size();

        Label stats = new Label(String.format("Rooms: %d | Customers: %d | Reservations: %d", roomCount, customerCount, reservationCount));
        stats.setFont(Font.font("Segoe UI", 14));
        stats.setTextFill(Color.web("#c0c0c0"));

        card.getChildren().addAll(welcome, stats);
        main.getChildren().addAll(heading, card);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showRoomManagement() {
        contentArea.getChildren().clear();

        VBox main = new VBox(20);
        main.setPadding(new Insets(24));

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Label("Room Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));

        Button addBtn = new Button("+ Add Room");
        addBtn.setStyle(BTN_STYLE);
        addBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(BTN_HOVER));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(BTN_STYLE));
        addBtn.setOnAction(e -> showAddRoomDialog());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #e8e8e8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> showRoomManagement());

        topBar.getChildren().addAll(heading, addBtn, refreshBtn);

        TableView<Room> table = new TableView<>();
        table.setPlaceholder(new Label("No rooms yet. Click 'Add Room' to add."));
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04);");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Room, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        TableColumn<Room, String> numCol = new TableColumn<>("Room #");
        numCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRoomType().toString()));
        TableColumn<Room, Double> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().toString()));

        table.getColumns().addAll(idCol, numCol, typeCol, priceCol, statusCol);

        ObservableList<Room> roomList = FXCollections.observableArrayList(RoomService.getInstance().findAll());
        table.setItems(roomList);

        ContextMenu ctx = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            Room sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                if (sel.getStatus() == RoomStatus.OCCUPIED) {
                    new Alert(Alert.AlertType.WARNING, "Cannot delete occupied room. Check out first.").show();
                    return;
                }
                if (new Alert(Alert.AlertType.CONFIRMATION, "Delete room " + sel.getRoomNumber() + "?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    RoomService.getInstance().deleteRoom(sel.getRoomId());
                    showRoomManagement();
                }
            }
        });
        ctx.getItems().add(deleteItem);
        table.setContextMenu(ctx);

        table.setPrefHeight(400);
        main.getChildren().addAll(topBar, table);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showAddRoomDialog() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Add Room");
        dialog.setHeaderText("Add a new room");

        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField roomNumber = new TextField();
        roomNumber.setPromptText("e.g. 101");
        ComboBox<RoomType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        typeCombo.setValue(RoomType.SINGLE);
        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 50.00");

        styleDialogField(roomNumber);
        styleDialogField(typeCombo);
        styleDialogField(priceField);

        addDialogRow(grid, 0, "Room number*:", roomNumber);
        addDialogRow(grid, 1, "Room type:", typeCombo);
        addDialogRow(grid, 2, "Price per night*:", priceField);

        dialog.getDialogPane().setContent(grid);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (roomNumber.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Room number required").showAndWait();
                ev.consume();
                return;
            }
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING, "Valid price required").showAndWait();
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == addType ? new Object() : null);
        dialog.showAndWait().ifPresent(v -> {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                int id = RoomService.getInstance().addRoom(roomNumber.getText().trim(), typeCombo.getValue(), price);
                if (id != -1) {
                    showRoomManagement();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Room number may already exist").show();
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid price").show();
            }
        });
    }

    private void showCustomerManagement() {
        contentArea.getChildren().clear();

        VBox main = new VBox(20);
        main.setPadding(new Insets(24));

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Label("Customer Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));

        Button addBtn = new Button("+ Add Customer");
        addBtn.setStyle(BTN_STYLE);
        addBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(BTN_HOVER));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(BTN_STYLE));
        addBtn.setOnAction(e -> showAddCustomerDialog());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #e8e8e8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> showCustomerManagement());

        topBar.getChildren().addAll(heading, addBtn, refreshBtn);

        TableView<Customer> table = new TableView<>();
        table.setPlaceholder(new Label("No customers yet. Click 'Add Customer'."));
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04);");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<Customer, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Customer, String> idNumCol = new TableColumn<>("ID Number");
        idNumCol.setCellValueFactory(new PropertyValueFactory<>("idNumber"));

        table.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, idNumCol);
        table.setItems(FXCollections.observableArrayList(CustomerService.getInstance().findAll()));

        ContextMenu ctx = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) showEditCustomerDialog(sel);
        });
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && new Alert(Alert.AlertType.CONFIRMATION, "Delete " + sel.getFullName() + "?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                CustomerService.getInstance().deleteCustomer(sel.getCustomerId());
                showCustomerManagement();
            }
        });
        ctx.getItems().addAll(editItem, deleteItem);
        table.setContextMenu(ctx);

        table.setPrefHeight(400);
        main.getChildren().addAll(topBar, table);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showAddCustomerDialog() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Add Customer");
        dialog.setHeaderText("Register a new customer");

        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField fullName = new TextField();
        fullName.setPromptText("Full name");
        TextField phone = new TextField();
        phone.setPromptText("Phone");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField idNumber = new TextField();
        idNumber.setPromptText("ID / Passport");

        styleDialogField(fullName);
        styleDialogField(phone);
        styleDialogField(email);
        styleDialogField(idNumber);

        addDialogRow(grid, 0, "Full name*:", fullName);
        addDialogRow(grid, 1, "Phone:", phone);
        addDialogRow(grid, 2, "Email:", email);
        addDialogRow(grid, 3, "ID/Passport:", idNumber);

        dialog.getDialogPane().setContent(grid);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (fullName.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Full name required").showAndWait();
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == addType ? new Object() : null);
        dialog.showAndWait().ifPresent(v -> {
            int id = CustomerService.getInstance().addCustomer(fullName.getText().trim(), phone.getText(), email.getText(), idNumber.getText());
            if (id != -1) showCustomerManagement();
        });
    }

    private void showEditCustomerDialog(Customer c) {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Edit Customer");
        dialog.setHeaderText("Edit: " + c.getFullName());

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField fullName = new TextField(c.getFullName());
        TextField phone = new TextField(c.getPhone());
        TextField email = new TextField(c.getEmail());
        TextField idNumber = new TextField(c.getIdNumber());

        styleDialogField(fullName);
        styleDialogField(phone);
        styleDialogField(email);
        styleDialogField(idNumber);

        addDialogRow(grid, 0, "Full name*:", fullName);
        addDialogRow(grid, 1, "Phone:", phone);
        addDialogRow(grid, 2, "Email:", email);
        addDialogRow(grid, 3, "ID/Passport:", idNumber);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (fullName.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Full name required").showAndWait();
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == saveType ? new Object() : null);
        int custId = c.getCustomerId();
        dialog.showAndWait().ifPresent(v -> {
            CustomerService.getInstance().updateCustomer(custId, fullName.getText().trim(), phone.getText(), email.getText(), idNumber.getText());
            showCustomerManagement();
        });
    }

    private void showReservationManagement() {
        contentArea.getChildren().clear();

        VBox main = new VBox(20);
        main.setPadding(new Insets(24));

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Label("Reservation Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));

        Button addBtn = new Button("+ New Reservation");
        addBtn.setStyle(BTN_STYLE);
        addBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(BTN_HOVER));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(BTN_STYLE));
        addBtn.setOnAction(e -> showAddReservationDialog());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #e8e8e8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> showReservationManagement());

        topBar.getChildren().addAll(heading, addBtn, refreshBtn);

        TableView<Reservation> table = new TableView<>();
        table.setPlaceholder(new Label("No reservations yet."));
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04);");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        TableColumn<Reservation, String> custCol = new TableColumn<>("Customer");
        custCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                CustomerService.getInstance().findById(c.getValue().getCustomerId()).map(Customer::getFullName).orElse("?")));
        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                RoomService.getInstance().findById(c.getValue().getRoomId()).map(Room::getRoomNumber).orElse("?")));
        TableColumn<Reservation, String> checkInCol = new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCheckInDate().toString()));
        TableColumn<Reservation, String> checkOutCol = new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCheckOutDate().toString()));
        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().toString()));

        TableColumn<Reservation, Integer> guestsCol = new TableColumn<>("Guests");
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        table.getColumns().addAll(idCol, custCol, roomCol, checkInCol, checkOutCol, guestsCol, statusCol);
        table.setItems(FXCollections.observableArrayList(ReservationService.getInstance().findAll()));

        ContextMenu ctx = new ContextMenu();
        MenuItem checkInItem = new MenuItem("Check In");
        checkInItem.setOnAction(e -> {
            Reservation sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && sel.getStatus() == ReservationStatus.BOOKED) {
                ReservationService.getInstance().checkIn(sel.getReservationId());
                showReservationManagement();
            }
        });
        MenuItem checkOutItem = new MenuItem("Check Out");
        checkOutItem.setOnAction(e -> {
            Reservation sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && sel.getStatus() == ReservationStatus.CHECKED_IN) {
                ReservationService.getInstance().checkOut(sel.getReservationId());
                showReservationManagement();
            }
        });
        MenuItem cancelItem = new MenuItem("Cancel");
        cancelItem.setOnAction(e -> {
            Reservation sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && new Alert(Alert.AlertType.CONFIRMATION, "Cancel reservation?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                ReservationService.getInstance().cancelReservation(sel.getReservationId());
                showReservationManagement();
            }
        });
        ctx.getItems().addAll(checkInItem, checkOutItem, cancelItem);
        table.setContextMenu(ctx);

        table.setPrefHeight(400);
        main.getChildren().addAll(topBar, table);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showAddReservationDialog() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("New Reservation");
        dialog.setHeaderText("Create a reservation");

        ButtonType addType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        List<Customer> customers = CustomerService.getInstance().findAll();
        ComboBox<Customer> custCombo = new ComboBox<>(FXCollections.observableArrayList(customers));
        custCombo.setConverter(new javafx.util.StringConverter<Customer>() {
            @Override
            public String toString(Customer c) { return c != null ? c.getFullName() + " (ID:" + c.getCustomerId() + ")" : ""; }
            @Override
            public Customer fromString(String s) { return null; }
        });
        if (!customers.isEmpty()) custCombo.setValue(customers.get(0));

        DatePicker checkInPicker = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> guestsSpinner = new Spinner<>(1, 20, 1);
        guestsSpinner.setEditable(true);

        ComboBox<String> roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select room");

        Label daysLabel = new Label("0 nights");
        styleDialogLabel(daysLabel);

        styleDialogField(custCombo);
        styleDialogField(checkInPicker);
        styleDialogField(checkOutPicker);
        styleDialogField(guestsSpinner);
        styleDialogField(roomCombo);

        addDialogRow(grid, 0, "Customer*:", custCombo);
        addDialogRow(grid, 1, "Check-in*:", checkInPicker);
        addDialogRow(grid, 2, "Check-out*:", checkOutPicker);
        addDialogRow(grid, 3, "Nights staying:", daysLabel);
        addDialogRow(grid, 4, "Number of guests*:", guestsSpinner);
        addDialogRow(grid, 5, "Room*:", roomCombo);

        Runnable updateRooms = () -> {
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (ci != null && co != null) {
                long days = ChronoUnit.DAYS.between(ci, co);
                daysLabel.setText(days + " night(s)");
            }
            updateAvailableRoomsForPickers(checkInPicker, checkOutPicker, roomCombo);
        };
        checkInPicker.valueProperty().addListener((o, old, val) -> updateRooms.run());
        checkOutPicker.valueProperty().addListener((o, old, val) -> updateRooms.run());
        updateRooms.run();

        TextField payAmountField = new TextField();
        payAmountField.setPromptText("Optional: amount to pay now");
        ComboBox<PaymentMethod> payMethodCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        payMethodCombo.setValue(PaymentMethod.CASH);
        styleDialogField(payAmountField);
        styleDialogField(payMethodCombo);
        addDialogRow(grid, 6, "Pay now (optional):", payAmountField);
        addDialogRow(grid, 7, "Payment method:", payMethodCombo);

        dialog.getDialogPane().setContent(grid);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (custCombo.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Select a customer").showAndWait();
                ev.consume();
                return;
            }
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (ci == null || co == null || !co.isAfter(ci)) {
                new Alert(Alert.AlertType.WARNING, "Valid dates required (check-out after check-in)").showAndWait();
                ev.consume();
                return;
            }
            Room sel = getSelectedRoom(roomCombo);
            if (sel == null) {
                new Alert(Alert.AlertType.WARNING, "Select an available room").showAndWait();
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == addType ? new Object() : null);
        dialog.showAndWait().ifPresent(v -> {
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            Room sel = getSelectedRoom(roomCombo);
            Customer cust = custCombo.getValue();
            int numGuests = guestsSpinner.getValue();
            if (cust != null && ci != null && co != null && sel != null) {
                int id = ReservationService.getInstance().createReservation(cust.getCustomerId(), sel.getRoomId(), ci, co, numGuests);
                if (id != -1) {
                    try {
                        double payAmount = Double.parseDouble(payAmountField.getText().trim());
                        if (payAmount > 0) {
                            PaymentService.getInstance().recordPayment(id, payAmount, payMethodCombo.getValue());
                        }
                    } catch (NumberFormatException ignored) { }
                    showReservationManagement();
                } else new Alert(Alert.AlertType.ERROR, "Room not available for these dates").show();
            }
        });
    }

    private void updateAvailableRoomsForPickers(DatePicker checkIn, DatePicker checkOut, ComboBox<String> roomCombo) {
        LocalDate ci = checkIn.getValue();
        LocalDate co = checkOut.getValue();
        roomCombo.getItems().clear();
        if (ci == null || co == null || !co.isAfter(ci)) return;
        List<Room> available = ReservationService.getInstance().getAvailableRooms(ci, co);
        for (Room r : available) {
            roomCombo.getItems().add(r.getRoomNumber() + " - " + r.getRoomType() + " - $" + r.getPricePerNight());
        }
        if (!available.isEmpty()) roomCombo.setValue(roomCombo.getItems().get(0));
    }

    private Room getSelectedRoom(ComboBox<String> roomCombo) {
        String val = roomCombo.getValue();
        if (val == null) return null;
        String roomNum = val.split(" - ")[0].trim();
        return RoomService.getInstance().findByRoomNumber(roomNum).orElse(null);
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void showPaymentManagement() {
        contentArea.getChildren().clear();

        VBox main = new VBox(20);
        main.setPadding(new Insets(24));

        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Label("Payment Management");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));

        Button addBtn = new Button("+ Record Payment");
        addBtn.setStyle(BTN_STYLE);
        addBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(BTN_HOVER));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(BTN_STYLE));
        addBtn.setOnAction(e -> showAddPaymentDialog());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #e8e8e8; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> showPaymentManagement());

        topBar.getChildren().addAll(heading, addBtn, refreshBtn);

        TableView<Payment> table = new TableView<>();
        table.setPlaceholder(new Label("No payments yet."));
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04);");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Payment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        TableColumn<Payment, Integer> resCol = new TableColumn<>("Reservation ID");
        resCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        TableColumn<Payment, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Payment, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMethod().toString()));
        TableColumn<Payment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentDate().toString()));

        table.getColumns().addAll(idCol, resCol, amountCol, methodCol, dateCol);
        List<Payment> paymentList = (SessionManager.isGuest())
                ? PaymentService.getInstance().findByCustomerId(SessionManager.getCurrentGuestId())
                : PaymentService.getInstance().findAll();
        table.setItems(FXCollections.observableArrayList(paymentList));

        table.setPrefHeight(400);
        main.getChildren().addAll(topBar, table);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showAddPaymentDialog() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Record Payment");
        dialog.setHeaderText("Record a payment for a reservation");

        ButtonType addType = new ButtonType("Record", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField resIdField = new TextField();
        resIdField.setPromptText("Reservation ID");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        ComboBox<PaymentMethod> methodCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        methodCombo.setValue(PaymentMethod.CASH);

        styleDialogField(resIdField);
        styleDialogField(amountField);
        styleDialogField(methodCombo);

        addDialogRow(grid, 0, "Reservation ID*:", resIdField);
        addDialogRow(grid, 1, "Amount*:", amountField);
        addDialogRow(grid, 2, "Method:", methodCombo);

        dialog.getDialogPane().setContent(grid);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            try {
                int resId = Integer.parseInt(resIdField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING, "Valid reservation ID and amount required").showAndWait();
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == addType ? new Object() : null);
        dialog.showAndWait().ifPresent(v -> {
            try {
                int resId = Integer.parseInt(resIdField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());
                int id = PaymentService.getInstance().recordPayment(resId, amount, methodCombo.getValue());
                if (id != -1) showPaymentManagement();
                else new Alert(Alert.AlertType.ERROR, "Reservation not found or invalid amount").show();
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid input").show();
            }
        });
    }

    private StackPane guestLoginCard, guestRegisterCard, adminLoginCard, adminRegisterCard;

    private VBox buildLandingPage() {
        VBox authContainer = new VBox(24);
        authContainer.setAlignment(Pos.CENTER);
        authContainer.setPadding(new Insets(48));

        Label title = new Label("Hotel Reservation System");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#e8e8e8"));

        Label sub = new Label("Book your stay with ease");
        sub.setFont(Font.font("Segoe UI", 18));
        sub.setTextFill(Color.web("#a0a0a0"));

        guestLoginCard = buildGuestLoginCard();
        guestRegisterCard = buildGuestRegisterCard();
        adminLoginCard = buildAdminLoginCard();
        adminRegisterCard = buildAdminRegisterCard();

        StackPane cardsStack = new StackPane();
        cardsStack.getChildren().addAll(adminRegisterCard, adminLoginCard, guestRegisterCard, guestLoginCard);
        guestRegisterCard.setVisible(false);
        adminLoginCard.setVisible(false);
        adminRegisterCard.setVisible(false);

        authContainer.getChildren().addAll(title, sub, new Region() {{ setPrefHeight(8); }}, cardsStack);
        return new VBox(authContainer);
    }

    private StackPane buildGuestLoginCard() {
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        styleField(usernameField);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        stylePassword(passwordField);
        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#e94560"));
        statusLabel.setVisible(false);
        Button signInBtn = styleButton(new Button("SIGN IN"));
        signInBtn.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();
            if (u.isEmpty() || p.isEmpty()) {
                statusLabel.setText("Please enter username and password");
                statusLabel.setVisible(true);
                return;
            }
            if (GuestAuthService.getInstance().login(u, p) != -1) switchToGuestDashboard();
            else { statusLabel.setText("Invalid username or password"); statusLabel.setVisible(true); }
        });
        Hyperlink toRegister = new Hyperlink("Create account");
        toRegister.setFont(Font.font("Segoe UI", 12));
        toRegister.setTextFill(Color.web("#e94560"));
        toRegister.setStyle("-fx-underline: false; -fx-cursor: hand;");
        toRegister.setOnAction(e -> {
            guestLoginCard.setVisible(false);
            guestRegisterCard.setVisible(true);
            adminLoginCard.setVisible(false);
            adminRegisterCard.setVisible(false);
        });
        Hyperlink toAdmin = new Hyperlink("Staff? Admin Login");
        toAdmin.setFont(Font.font("Segoe UI", 11));
        toAdmin.setTextFill(Color.web("#a0a0a0"));
        toAdmin.setStyle("-fx-underline: false; -fx-cursor: hand;");
        toAdmin.setOnAction(e -> {
            guestLoginCard.setVisible(false);
            guestRegisterCard.setVisible(false);
            adminLoginCard.setVisible(true);
            adminRegisterCard.setVisible(false);
        });
        return buildAuthCard("Guest Login", "Sign in to book and manage reservations",
                usernameField, passwordField, statusLabel, signInBtn, toRegister, toAdmin);
    }

    private StackPane buildGuestRegisterCard() {
        TextField u = new TextField();
        u.setPromptText("Username");
        TextField fn = new TextField();
        fn.setPromptText("Full name");
        TextField ph = new TextField();
        ph.setPromptText("Phone");
        TextField em = new TextField();
        em.setPromptText("Email");
        PasswordField p = new PasswordField();
        p.setPromptText("Password");
        PasswordField cp = new PasswordField();
        cp.setPromptText("Confirm password");
        styleField(u);
        styleField(fn);
        styleField(ph);
        styleField(em);
        stylePassword(p);
        stylePassword(cp);
        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#e94560"));
        statusLabel.setVisible(false);
        Button createBtn = styleButton(new Button("CREATE ACCOUNT"));
        createBtn.setOnAction(e -> {
            if (!p.getText().equals(cp.getText())) { statusLabel.setText("Passwords do not match"); statusLabel.setVisible(true); return; }
            if (GuestAuthService.getInstance().register(u.getText().trim(), fn.getText().trim(), ph.getText(), em.getText(), null, p.getText()) != -1) switchToGuestDashboard();
            else { statusLabel.setText("Username may already exist"); statusLabel.setVisible(true); }
        });
        Hyperlink toLogin = new Hyperlink("Already have an account? Sign in");
        toLogin.setFont(Font.font("Segoe UI", 12));
        toLogin.setTextFill(Color.web("#e94560"));
        toLogin.setStyle("-fx-underline: false; -fx-cursor: hand;");
        toLogin.setOnAction(e -> {
            guestLoginCard.setVisible(true);
            guestRegisterCard.setVisible(false);
            adminLoginCard.setVisible(false);
            adminRegisterCard.setVisible(false);
        });
        Label title = new Label("Create Guest Account");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#e8e8e8"));
        Label sub = new Label("Join us to book rooms");
        sub.setFont(Font.font("Segoe UI", 14));
        sub.setTextFill(Color.web("#a0a0a0"));
        VBox inner = new VBox(12);
        inner.setAlignment(Pos.CENTER);
        inner.getChildren().addAll(title, sub, new Region() {{ setPrefHeight(4); }}, u, fn, ph, em, p, cp, statusLabel, createBtn, toLogin);
        return buildCard(inner);
    }

    private StackPane buildAdminLoginCard() {
        TextField u = new TextField();
        u.setPromptText("Username");
        PasswordField p = new PasswordField();
        p.setPromptText("Password");
        styleField(u);
        stylePassword(p);
        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#e94560"));
        statusLabel.setVisible(false);
        Button signInBtn = styleButton(new Button("SIGN IN"));
        signInBtn.setOnAction(e -> {
            if (AuthService.getInstance().login(u.getText().trim(), p.getText()) != -1) switchToAdminDashboard();
            else { statusLabel.setText("Invalid username or password"); statusLabel.setVisible(true); }
        });
        Hyperlink toRegister = new Hyperlink("Create admin account");
        toRegister.setFont(Font.font("Segoe UI", 12));
        toRegister.setTextFill(Color.web("#e94560"));
        toRegister.setStyle("-fx-underline: false; -fx-cursor: hand;");
        toRegister.setOnAction(e -> {
            guestLoginCard.setVisible(false);
            guestRegisterCard.setVisible(false);
            adminLoginCard.setVisible(false);
            adminRegisterCard.setVisible(true);
        });
        Hyperlink toGuest = new Hyperlink("Back to Guest");
        toGuest.setFont(Font.font("Segoe UI", 11));
        toGuest.setTextFill(Color.web("#a0a0a0"));
        toGuest.setStyle("-fx-underline: false; -fx-cursor: hand;");
        toGuest.setOnAction(e -> {
            guestLoginCard.setVisible(true);
            guestRegisterCard.setVisible(false);
            adminLoginCard.setVisible(false);
            adminRegisterCard.setVisible(false);
        });
        return buildAuthCard("Admin Login", "Staff sign in", u, p, statusLabel, signInBtn, toRegister, toGuest);
    }

    private StackPane buildAdminRegisterCard() {
        TextField u = new TextField();
        u.setPromptText("Username");
        TextField fn = new TextField();
        fn.setPromptText("Full name");
        PasswordField p = new PasswordField();
        p.setPromptText("Password");
        PasswordField cp = new PasswordField();
        cp.setPromptText("Confirm password");
        styleField(u);
        styleField(fn);
        stylePassword(p);
        stylePassword(cp);
        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#e94560"));
        statusLabel.setVisible(false);
        Button createBtn = styleButton(new Button("CREATE ADMIN ACCOUNT"));
        createBtn.setOnAction(e -> {
            if (!p.getText().equals(cp.getText())) { statusLabel.setText("Passwords do not match"); statusLabel.setVisible(true); return; }
            if (AuthService.getInstance().register(u.getText().trim(), fn.getText().trim(), p.getText()) != -1) switchToAdminDashboard();
            else { statusLabel.setText("Username may already exist"); statusLabel.setVisible(true); }
        });
        Hyperlink toLogin = new Hyperlink("Already have an account? Sign in");
        toLogin.setFont(Font.font("Segoe UI", 12));
        toLogin.setTextFill(Color.web("#e94560"));
        toLogin.setStyle("-fx-underline: false; -fx-cursor: hand;");
        toLogin.setOnAction(e -> {
            guestLoginCard.setVisible(false);
            guestRegisterCard.setVisible(false);
            adminLoginCard.setVisible(true);
            adminRegisterCard.setVisible(false);
        });
        Label title = new Label("Create Admin Account");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#e8e8e8"));
        Label sub = new Label("Register staff account");
        sub.setFont(Font.font("Segoe UI", 14));
        sub.setTextFill(Color.web("#a0a0a0"));
        VBox inner = new VBox(12);
        inner.setAlignment(Pos.CENTER);
        inner.getChildren().addAll(title, sub, new Region() {{ setPrefHeight(4); }}, u, fn, p, cp, statusLabel, createBtn, toLogin);
        return buildCard(inner);
    }

    private StackPane buildAuthCard(String titleText, String subText, TextField username, PasswordField password, Label status, Button actionBtn, Hyperlink link1, Hyperlink link2) {
        VBox inner = new VBox(12);
        inner.setAlignment(Pos.CENTER);
        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#e8e8e8"));
        Label sub = new Label(subText);
        sub.setFont(Font.font("Segoe UI", 14));
        sub.setTextFill(Color.web("#a0a0a0"));
        inner.getChildren().addAll(title, sub, new Region() {{ setPrefHeight(4); }}, username, password, status, actionBtn, link1, link2);
        return buildCard(inner);
    }

    private StackPane buildCard(javafx.scene.Node content) {
        StackPane card = new StackPane();
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.06); -fx-background-radius: 20; -fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-radius: 20; -fx-border-width: 1;");
        card.setPadding(new Insets(40));
        card.setEffect(new DropShadow(30, 0, 10, Color.web("#e94560", 0.3)));
        card.getChildren().add(content);
        return card;
    }

    private void showGuestHome() {
        contentArea.getChildren().clear();
        VBox main = new VBox(20);
        main.setPadding(new Insets(24));
        Label heading = new Label("Welcome, " + SessionManager.getCurrentDisplayName() + "!");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));
        Label sub = new Label("Book a room, view your reservations, and manage payments.");
        sub.setFont(Font.font("Segoe UI", 14));
        sub.setTextFill(Color.web("#c0c0c0"));
        main.getChildren().addAll(heading, sub);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showGuestBookRoom() {
        contentArea.getChildren().clear();
        VBox main = new VBox(20);
        main.setPadding(new Insets(24));
        Label heading = new Label("Book a Room");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));
        Button bookBtn = new Button("+ New Booking");
        bookBtn.setStyle(BTN_STYLE);
        bookBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        bookBtn.setOnMouseEntered(e -> bookBtn.setStyle(BTN_HOVER));
        bookBtn.setOnMouseExited(e -> bookBtn.setStyle(BTN_STYLE));
        bookBtn.setOnAction(e -> showGuestBookDialog());
        main.getChildren().add(heading);
        main.getChildren().add(bookBtn);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showGuestBookDialog() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Book a Room");
        dialog.setHeaderText("Create your reservation");
        ButtonType addType = new ButtonType("Book", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        DatePicker checkInPicker = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> guestsSpinner = new Spinner<>(1, 20, 1);
        guestsSpinner.setEditable(true);
        ComboBox<String> roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select room");
        Label daysLabel = new Label("0 nights");
        styleDialogLabel(daysLabel);
        TextField payAmountField = new TextField();
        payAmountField.setPromptText("Optional: amount to pay now");
        ComboBox<PaymentMethod> payMethodCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        payMethodCombo.setValue(PaymentMethod.CASH);

        styleDialogField(checkInPicker);
        styleDialogField(checkOutPicker);
        styleDialogField(guestsSpinner);
        styleDialogField(roomCombo);
        styleDialogField(payAmountField);
        styleDialogField(payMethodCombo);

        addDialogRow(grid, 0, "Check-in*:", checkInPicker);
        addDialogRow(grid, 1, "Check-out*:", checkOutPicker);
        addDialogRow(grid, 2, "Nights staying:", daysLabel);
        addDialogRow(grid, 3, "Number of guests*:", guestsSpinner);
        addDialogRow(grid, 4, "Room*:", roomCombo);
        addDialogRow(grid, 5, "Pay now (optional):", payAmountField);
        addDialogRow(grid, 6, "Payment method:", payMethodCombo);

        Runnable updateRooms = () -> {
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (ci != null && co != null) daysLabel.setText(ChronoUnit.DAYS.between(ci, co) + " night(s)");
            updateAvailableRoomsForPickers(checkInPicker, checkOutPicker, roomCombo);
        };
        checkInPicker.valueProperty().addListener((o, a, v) -> updateRooms.run());
        checkOutPicker.valueProperty().addListener((o, a, v) -> updateRooms.run());
        updateRooms.run();

        dialog.getDialogPane().setContent(grid);
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (ci == null || co == null || !co.isAfter(ci)) {
                new Alert(Alert.AlertType.WARNING, "Valid dates required").showAndWait();
                ev.consume();
            } else if (getSelectedRoom(roomCombo) == null) {
                new Alert(Alert.AlertType.WARNING, "Select a room").showAndWait();
                ev.consume();
            }
        });
        dialog.setResultConverter(btn -> btn == addType ? new Object() : null);
        int guestId = SessionManager.getCurrentGuestId();
        dialog.showAndWait().ifPresent(v -> {
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            Room sel = getSelectedRoom(roomCombo);
            if (guestId != -1 && ci != null && co != null && sel != null) {
                int id = ReservationService.getInstance().createReservation(guestId, sel.getRoomId(), ci, co, guestsSpinner.getValue());
                if (id != -1) {
                    try {
                        double pay = Double.parseDouble(payAmountField.getText().trim());
                        if (pay > 0) PaymentService.getInstance().recordPayment(id, pay, payMethodCombo.getValue());
                    } catch (NumberFormatException ignored) { }
                    showGuestReservations();
                }
            }
        });
    }

    private void showGuestReservations() {
        contentArea.getChildren().clear();
        VBox main = new VBox(20);
        main.setPadding(new Insets(24));
        Label heading = new Label("My Reservations");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));
        Button bookBtn = new Button("+ Book Another");
        bookBtn.setStyle(BTN_STYLE);
        bookBtn.setOnMouseEntered(e -> bookBtn.setStyle(BTN_HOVER));
        bookBtn.setOnMouseExited(e -> bookBtn.setStyle(BTN_STYLE));
        bookBtn.setOnAction(e -> showGuestBookDialog());
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(heading, bookBtn);

        TableView<Reservation> table = new TableView<>();
        table.setPlaceholder(new Label("No reservations yet. Click 'Book a Room' to make one."));
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04);");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                RoomService.getInstance().findById(c.getValue().getRoomId()).map(Room::getRoomNumber).orElse("?")));
        TableColumn<Reservation, String> checkInCol = new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCheckInDate().toString()));
        TableColumn<Reservation, String> checkOutCol = new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCheckOutDate().toString()));
        TableColumn<Reservation, Integer> guestsCol = new TableColumn<>("Guests");
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().toString()));
        table.getColumns().addAll(idCol, roomCol, checkInCol, checkOutCol, guestsCol, statusCol);
        table.setItems(FXCollections.observableArrayList(ReservationService.getInstance().findByCustomerId(SessionManager.getCurrentGuestId())));

        ContextMenu ctx = new ContextMenu();
        MenuItem cancelItem = new MenuItem("Cancel");
        cancelItem.setOnAction(e -> {
            Reservation sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && sel.getStatus() == ReservationStatus.BOOKED && new Alert(Alert.AlertType.CONFIRMATION, "Cancel reservation?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                ReservationService.getInstance().cancelReservation(sel.getReservationId());
                showGuestReservations();
            }
        });
        ctx.getItems().add(cancelItem);
        table.setContextMenu(ctx);
        table.setPrefHeight(400);
        main.getChildren().addAll(top, table);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showGuestPayments() {
        contentArea.getChildren().clear();
        VBox main = new VBox(20);
        main.setPadding(new Insets(24));
        Label heading = new Label("My Payments");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));
        Button addBtn = new Button("+ Make Payment");
        addBtn.setStyle(BTN_STYLE);
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(BTN_HOVER));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(BTN_STYLE));
        addBtn.setOnAction(e -> showAddPaymentDialog());
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(heading, addBtn);

        TableView<Payment> table = new TableView<>();
        table.setPlaceholder(new Label("No payments yet."));
        table.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04);");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Payment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        TableColumn<Payment, Integer> resCol = new TableColumn<>("Reservation ID");
        resCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        TableColumn<Payment, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Payment, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMethod().toString()));
        TableColumn<Payment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPaymentDate().toString()));
        table.getColumns().addAll(idCol, resCol, amountCol, methodCol, dateCol);
        table.setItems(FXCollections.observableArrayList(PaymentService.getInstance().findByCustomerId(SessionManager.getCurrentGuestId())));
        table.setPrefHeight(400);
        main.getChildren().addAll(top, table);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    private void showGuestProfile() {
        contentArea.getChildren().clear();
        Customer c = CustomerService.getInstance().findById(SessionManager.getCurrentGuestId()).orElse(null);
        if (c == null) return;
        VBox main = new VBox(20);
        main.setPadding(new Insets(24));
        Label heading = new Label("Profile");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web("#e8e8e8"));
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.06); -fx-background-radius: 12;");
        card.getChildren().addAll(
                new Label("Name: " + c.getFullName()),
                new Label("Phone: " + (c.getPhone() != null ? c.getPhone() : "-")),
                new Label("Email: " + (c.getEmail() != null ? c.getEmail() : "-"))
        );
        for (javafx.scene.Node n : card.getChildren()) {
            if (n instanceof Label) {
                ((Label) n).setFont(Font.font("Segoe UI", 14));
                ((Label) n).setTextFill(Color.web("#c0c0c0"));
            }
        }
        main.getChildren().addAll(heading, card);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to initialize database: " + e.getMessage()).showAndWait();
            return;
        }

        primaryStage.setTitle("Hotel Reservation System");
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0f0f14;");

        Rectangle bg = new Rectangle(1920, 1080);
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a1a2e")),
                new Stop(0.5, Color.web("#16213e")),
                new Stop(1, Color.web("#0f3460"))
        );
        bg.setFill(gradient);
        bg.widthProperty().bind(root.widthProperty());
        bg.heightProperty().bind(root.heightProperty());

        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(bg, mainLayout);

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(24));

        landingView = buildLandingPage();
        mainLayout.setCenter(landingView);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
