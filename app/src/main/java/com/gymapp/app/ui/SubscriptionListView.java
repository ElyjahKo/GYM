package com.gymapp.app.ui;

import com.gymapp.app.data.Subscription;
import com.gymapp.app.data.SubscriptionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SubscriptionListView {
    private final SubscriptionService service = new SubscriptionService();
    private final BorderPane root = new BorderPane();

    private final TableView<Subscription> table = new TableView<>();
    private final ObservableList<Subscription> rows = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final Pagination pagination = new Pagination();

    private int pageSize = 20;

    public SubscriptionListView() {
        root.setStyle("-fx-background-color: #f5f5f5;");
        build();
        loadPage(1);
    }

    private void build() {
        root.setPadding(new Insets(20));

        // Header
        VBox header = new VBox(15);
        Label titleLabel = new Label("📋 Liste des Abonnements");
        titleLabel.getStyleClass().add("header-title");

        // Filter Card
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.getStyleClass().add("card");

        Label statusLabel = new Label("🔍 Statut:");
        statusLabel.getStyleClass().add("subheader");
        
        statusFilter.getItems().addAll("Tous", "Actif", "Expiré");
        statusFilter.getSelectionModel().select("Tous");
        statusFilter.setPrefWidth(150);
        statusFilter.setStyle("-fx-background-radius: 8; -fx-padding: 8;");
        
        searchField.setPromptText("🔎 Rechercher un client...");
        searchField.setPrefWidth(300);
        
        Button searchBtn = new Button("🔍 Rechercher");
        searchBtn.getStyleClass().addAll("button", "btn-primary");
        searchBtn.setOnAction(e -> loadPage(1));
        
        filterBox.getChildren().addAll(statusLabel, statusFilter, searchField, searchBtn);

        header.getChildren().addAll(titleLabel, filterBox);
        root.setTop(header);

        // Table Card
        VBox tableCard = new VBox(15);
        tableCard.getStyleClass().add("card");
        tableCard.setSpacing(15);
        tableCard.setPadding(new Insets(20));

        // Table columns with icons and better styling
        TableColumn<Subscription, String> genderCol = new TableColumn<>("👤 Genre");
        genderCol.setCellValueFactory(c -> {
            Subscription s = c.getValue();
            String val = "";
            if (s != null && s.getMember() != null) {
                String g = s.getMember().getGender();
                if ("H".equalsIgnoreCase(g)) val = "👨 Homme";
                else if ("F".equalsIgnoreCase(g)) val = "👩 Femme";
            }
            return new SimpleStringProperty(val);
        });
        genderCol.setPrefWidth(120);
        
        TableColumn<Subscription, String> clientCol = new TableColumn<>("📝 Client");
        clientCol.setCellValueFactory(c -> {
            Subscription s = c.getValue();
            String val = (s != null && s.getMember() != null && s.getMember().getFullName() != null)
                    ? s.getMember().getFullName() : "";
            return new SimpleStringProperty(val);
        });
        clientCol.setPrefWidth(200);
        
        TableColumn<Subscription, String> planCol = new TableColumn<>("📦 Forfait");
        planCol.setCellValueFactory(c -> {
            Subscription s = c.getValue();
            String val = (s != null && s.getPlan() != null) ? s.getPlan() : "";
            return new SimpleStringProperty(val);
        });
        planCol.setPrefWidth(150);
        
        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
        
        TableColumn<Subscription, String> startCol = new TableColumn<>("📅 Début");
        startCol.setCellValueFactory(c -> {
            Subscription s = c.getValue();
            String val = (s != null && s.getStartAt() != null) ? df.format(s.getStartAt()) : "";
            return new SimpleStringProperty(val);
        });
        startCol.setPrefWidth(120);
        
        TableColumn<Subscription, String> endCol = new TableColumn<>("📅 Fin");
        endCol.setCellValueFactory(c -> {
            Subscription s = c.getValue();
            String val = (s != null && s.getEndAt() != null) ? df.format(s.getEndAt()) : "";
            return new SimpleStringProperty(val);
        });
        endCol.setPrefWidth(120);
        
        TableColumn<Subscription, String> statusCol = new TableColumn<>("📊 Statut");
        statusCol.setCellValueFactory(c -> {
            Subscription s = c.getValue();
            String val = (s != null && s.getEndAt() != null && s.getEndAt().isBefore(java.time.LocalDate.now())) ? "Expiré" : "Actif";
            return new SimpleStringProperty(val);
        });
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(genderCol, clientCol, planCol, startCol, endCol, statusCol);
        table.setItems(rows);
        table.setPlaceholder(new Label("Aucun abonnement trouvé"));
        
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().add(table);
        
        root.setCenter(tableCard);

        // Temporarily remove Pagination from layout to stabilize JavaFX rendering
        // Manual navigation can be reintroduced later if needed
    }

    private void loadPage(int page) {
        String filter = statusFilter.getValue();
        String status = null;
        if ("Actif".equalsIgnoreCase(filter)) status = "active";
        else if ("Expiré".equalsIgnoreCase(filter)) status = "expired";
        String q = searchField.getText();
        List<Subscription> list = service.listPaged(page, pageSize, status, q);
        rows.setAll(list);
        int pageCount = list.size() < pageSize ? page : page + 1;
        pagination.setCurrentPageIndex(Math.max(0, page - 1));
        pagination.setPageCount(Math.max(1, pageCount));
    }

    public Node getRoot() { return root; }
}