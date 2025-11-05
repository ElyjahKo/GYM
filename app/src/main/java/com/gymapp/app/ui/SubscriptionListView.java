package com.gymapp.app.ui;

import com.gymapp.app.data.Subscription;
import com.gymapp.app.data.SubscriptionService;
import javafx.animation.*;
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
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SubscriptionListView {
    private final SubscriptionService service = new SubscriptionService();
    private final BorderPane root = new BorderPane();

    private final TableView<Subscription> table = new TableView<>();
    private final ObservableList<Subscription> rows = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> planFilter = new ComboBox<>();
    private final Label statsLabel = new Label();
    private final StackPane toastContainer = new StackPane();

    private int pageSize = 20;
    private int currentPage = 1;
    private final Label pageInfo = new Label();
    private final Button prevBtn = new Button();
    private final Button nextBtn = new Button();
    private final Button firstBtn = new Button();
    private final Button lastBtn = new Button();

    public SubscriptionListView() {
        root.setStyle("-fx-background-color: transparent;");
        build();
        loadPage(1);
    }

    private void build() {
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(25));
        
        // === HEADER ===
        VBox header = createHeader();
        
        // === FILTERS CARD ===
        VBox filtersCard = createFiltersCard();
        
        // === STATS CARDS ===
        HBox statsCards = createStatsCards();
        
        // === TABLE CARD ===
        VBox tableCard = createTableCard();
        
        // === PAGINATION ===
        HBox pagination = createPagination();
        
        mainContainer.getChildren().addAll(header, filtersCard, statsCards, tableCard, pagination);
        
        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        root.setCenter(scroll);
        
        // Toast container
        toastContainer.setAlignment(Pos.TOP_RIGHT);
        toastContainer.setPadding(new Insets(20));
        toastContainer.setPickOnBounds(false);
        root.setTop(toastContainer);
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon icon = new FontIcon(FontAwesomeSolid.LIST_ALT);
        icon.setIconSize(32);
        icon.setIconColor(Color.web("#667eea"));
        
        Label title = new Label("Liste des Abonnements");
        title.getStyleClass().add("header-title");
        title.setGraphic(icon);
        title.setContentDisplay(ContentDisplay.LEFT);
        title.setGraphicTextGap(15);
        
        titleBox.getChildren().add(title);
        
        statsLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        statsLabel.setTextFill(Color.web("#a0aec0"));
        
        header.getChildren().addAll(titleBox, statsLabel);
        return header;
    }

    private VBox createFiltersCard() {
        VBox filtersCard = new VBox(20);
        filtersCard.getStyleClass().add("card");
        
        Label filterTitle = new Label("🔍 Filtres de Recherche");
        filterTitle.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        filterTitle.setTextFill(Color.WHITE);
        
        GridPane filtersGrid = new GridPane();
        filtersGrid.setHgap(15);
        filtersGrid.setVgap(15);
        
        // Search field
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon searchIcon = new FontIcon(FontAwesomeSolid.SEARCH);
        searchIcon.setIconSize(16);
        searchIcon.setIconColor(Color.web("#667eea"));
        
        searchField.setPromptText("Rechercher un client...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        searchBox.getChildren().addAll(searchIcon, searchField);
        
        // Status filter
        VBox statusBox = createFilterBox("📊 Statut", statusFilter);
        statusFilter.getItems().addAll("Tous", "Actif", "Expiré");
        statusFilter.getSelectionModel().select("Tous");
        statusFilter.setPrefWidth(150);
        
        // Plan filter
        VBox planBox = createFilterBox("📋 Forfait", planFilter);
        planFilter.getItems().addAll("Tous", "Mensuel", "Trimestriel", "Annuel");
        planFilter.getSelectionModel().select("Tous");
        planFilter.setPrefWidth(150);
        
        // Search button
        Button searchBtn = createIconButton("Rechercher", FontAwesomeSolid.SEARCH, "btn-primary");
        searchBtn.setOnAction(e -> {
            // Rotation animation on icon
            FontIcon btnIcon = (FontIcon) searchBtn.getGraphic();
            RotateTransition rt = new RotateTransition(Duration.millis(500), btnIcon);
            rt.setByAngle(360);
            rt.play();
            loadPage(1);
        });
        
        Button resetBtn = createIconButton("Réinitialiser", FontAwesomeSolid.REDO, "btn-secondary");
        resetBtn.setOnAction(e -> resetFilters());
        
        filtersGrid.add(searchBox, 0, 0, 2, 1);
        filtersGrid.add(statusBox, 0, 1);
        filtersGrid.add(planBox, 1, 1);
        filtersGrid.add(searchBtn, 0, 2);
        filtersGrid.add(resetBtn, 1, 2);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        filtersGrid.getColumnConstraints().addAll(col1, col2);
        
        filtersCard.getChildren().addAll(filterTitle, filtersGrid);
        return filtersCard;
    }

    private VBox createFilterBox(String label, ComboBox<String> combo) {
        VBox box = new VBox(8);
        
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 13));
        lbl.setTextFill(Color.web("#a0aec0"));
        
        box.getChildren().addAll(lbl, combo);
        return box;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        
        VBox totalCard = createMiniStatCard("📊", "Total", "0", "#667eea");
        VBox activeCard = createMiniStatCard("✓", "Actifs", "0", "#00f2c3");
        VBox expiredCard = createMiniStatCard("✗", "Expirés", "0", "#ff6b9d");
        VBox revenueCard = createMiniStatCard("💰", "Revenus", "0 FCFA", "#feca57");
        
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(activeCard, Priority.ALWAYS);
        HBox.setHgrow(expiredCard, Priority.ALWAYS);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        
        statsBox.getChildren().addAll(totalCard, activeCard, expiredCard, revenueCard);
        return statsBox;
    }

    private VBox createMiniStatCard(String emoji, String label, String value, String accentColor) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPrefHeight(120);
        card.setStyle(card.getStyle() + "-fx-border-color: " + accentColor + "40; -fx-border-width: 0 0 3 0;");
        
        Label icon = new Label(emoji);
        icon.setFont(Font.font(28));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(accentColor));
        valueLabel.setId(label.toLowerCase() + "Value");
        
        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        descLabel.setTextFill(Color.web("#a0aec0"));
        
        card.getChildren().addAll(icon, valueLabel, descLabel);
        
        // Hover animation
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        return card;
    }

    private VBox createTableCard() {
        VBox tableCard = new VBox(15);
        tableCard.getStyleClass().add("card");
        
        HBox tableHeader = new HBox(15);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon tableIcon = new FontIcon(FontAwesomeSolid.TABLE);
        tableIcon.setIconSize(18);
        tableIcon.setIconColor(Color.web("#667eea"));
        
        Label tableTitle = new Label("📋 Résultats");
        tableTitle.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.WHITE);
        tableTitle.setGraphic(tableIcon);
        tableTitle.setContentDisplay(ContentDisplay.LEFT);
        
        Button exportBtn = createIconButton("Exporter", FontAwesomeSolid.FILE_EXPORT, "btn-secondary");
        exportBtn.setOnAction(e -> showToast("📊 Fonctionnalité d'export en cours de développement", "info"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        tableHeader.getChildren().addAll(tableTitle, spacer, exportBtn);
        
        // Table columns
        TableColumn<Subscription, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getId() != null ? "#" + c.getValue().getId() : ""));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-font-weight: 700; -fx-text-fill: #667eea;");
        
        TableColumn<Subscription, String> genderCol = new TableColumn<>("👤");
        genderCol.setCellValueFactory(c -> {
            String val = "";
            if (c.getValue() != null && c.getValue().getMember() != null) {
                String g = c.getValue().getMember().getGender();
                if ("H".equalsIgnoreCase(g)) val = "👨";
                else if ("F".equalsIgnoreCase(g)) val = "👩";
            }
            return new SimpleStringProperty(val);
        });
        genderCol.setPrefWidth(50);
        genderCol.setStyle("-fx-font-size: 18px; -fx-alignment: center;");
        
        TableColumn<Subscription, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(c -> {
            String val = (c.getValue() != null && c.getValue().getMember() != null 
                && c.getValue().getMember().getFullName() != null)
                    ? c.getValue().getMember().getFullName() : "";
            return new SimpleStringProperty(val);
        });
        clientCol.setPrefWidth(200);
        clientCol.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        
        TableColumn<Subscription, String> planCol = new TableColumn<>("📦 Forfait");
        planCol.setCellValueFactory(c -> {
            String val = (c.getValue() != null && c.getValue().getPlan() != null) 
                ? c.getValue().getPlan() : "";
            return new SimpleStringProperty(val);
        });
        planCol.setPrefWidth(120);
        
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        TableColumn<Subscription, String> startCol = new TableColumn<>("📅 Début");
        startCol.setCellValueFactory(c -> {
            String val = (c.getValue() != null && c.getValue().getStartAt() != null) 
                ? df.format(c.getValue().getStartAt()) : "";
            return new SimpleStringProperty(val);
        });
        startCol.setPrefWidth(110);
        
        TableColumn<Subscription, String> endCol = new TableColumn<>("📅 Fin");
        endCol.setCellValueFactory(c -> {
            String val = (c.getValue() != null && c.getValue().getEndAt() != null) 
                ? df.format(c.getValue().getEndAt()) : "";
            return new SimpleStringProperty(val);
        });
        endCol.setPrefWidth(110);
        
        TableColumn<Subscription, String> priceCol = new TableColumn<>("💰 Prix");
        priceCol.setCellValueFactory(c -> {
            String val = (c.getValue() != null && c.getValue().getPrice() != null)
                ? String.format("%,d FCFA", c.getValue().getPrice()) : "0 FCFA";
            return new SimpleStringProperty(val);
        });
        priceCol.setPrefWidth(120);
        priceCol.setStyle("-fx-font-weight: 700; -fx-text-fill: #feca57;");
        
        TableColumn<Subscription, String> statusCol = new TableColumn<>("📊 Statut");
        statusCol.setCellValueFactory(c -> {
            String val = (c.getValue() != null && c.getValue().getEndAt() != null 
                && c.getValue().getEndAt().isBefore(LocalDate.now())) ? "Expiré" : "Actif";
            return new SimpleStringProperty(val);
        });
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                if ("Actif".equals(item)) {
                    badge.getStyleClass().add("badge-success");
                    badge.setText("✓ " + item);
                } else {
                    badge.getStyleClass().add("badge-danger");
                    badge.setText("✗ " + item);
                }
                setGraphic(badge);
            }
        });

        table.getColumns().setAll(idCol, genderCol, clientCol, planCol, startCol, endCol, priceCol, statusCol);
        table.setItems(rows);
        table.setPlaceholder(new Label("Aucun abonnement trouvé"));
        table.setPrefHeight(450);
        
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(tableHeader, table);
        
        return tableCard;
    }

    private HBox createPagination() {
        HBox pager = new HBox(15);
        pager.setAlignment(Pos.CENTER);
        pager.getStyleClass().add("card");
        
        firstBtn.getStyleClass().addAll("button", "btn-secondary");
        firstBtn.setText("⏮");
        firstBtn.setPrefSize(45, 40);
        firstBtn.setOnAction(e -> loadPage(1));
        
        prevBtn.getStyleClass().addAll("button", "btn-secondary");
        prevBtn.setText("◀");
        prevBtn.setPrefSize(45, 40);
        prevBtn.setOnAction(e -> { if (currentPage > 1) loadPage(currentPage - 1); });
        
        pageInfo.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        pageInfo.setTextFill(Color.WHITE);
        pageInfo.setPrefWidth(200);
        pageInfo.setAlignment(Pos.CENTER);
        
        nextBtn.getStyleClass().addAll("button", "btn-primary");
        nextBtn.setText("▶");
        nextBtn.setPrefSize(45, 40);
        nextBtn.setOnAction(e -> loadPage(currentPage + 1));
        
        lastBtn.getStyleClass().addAll("button", "btn-primary");
        lastBtn.setText("⏭");
        lastBtn.setPrefSize(45, 40);
        lastBtn.setOnAction(e -> {
            // Estimate last page
            int estimated = (int) Math.ceil(rows.size() / (double) pageSize);
            if (estimated > currentPage) loadPage(estimated);
        });
        
        pager.getChildren().addAll(firstBtn, prevBtn, pageInfo, nextBtn, lastBtn);
        return pager;
    }

    private Button createIconButton(String text, FontAwesomeSolid iconType, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", styleClass);
        
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(14);
        btn.setGraphic(icon);
        
        return btn;
    }

    private void loadPage(int page) {
        String filter = statusFilter.getValue();
        String status = null;
        if ("Actif".equalsIgnoreCase(filter)) status = "active";
        else if ("Expiré".equalsIgnoreCase(filter)) status = "expired";
        
        String q = searchField.getText();
        List<Subscription> list = service.listPaged(page, pageSize, status, q);
        rows.setAll(list);
        currentPage = Math.max(1, page);
        
        // Update pagination UI
        pageInfo.setText(String.format("Page %d • %d résultats", currentPage, rows.size()));
        firstBtn.setDisable(currentPage <= 1);
        prevBtn.setDisable(currentPage <= 1);
        boolean hasNext = list != null && list.size() >= pageSize;
        nextBtn.setDisable(!hasNext);
        lastBtn.setDisable(!hasNext);
        
        updateStats(list);
    }

    private void updateStats(List<Subscription> list) {
        int total = list != null ? list.size() : 0;
        int active = 0;
        int expired = 0;
        long revenue = 0;
        
        if (list != null) {
            for (Subscription s : list) {
                if (s.getEndAt() != null && s.getEndAt().isBefore(LocalDate.now())) {
                    expired++;
                } else {
                    active++;
                }
                if (s.getPrice() != null) {
                    revenue += s.getPrice();
                }
            }
        }
        
        updateStatCard("total", String.valueOf(total));
        updateStatCard("actifs", String.valueOf(active));
        updateStatCard("expirés", String.valueOf(expired));
        updateStatCard("revenus", String.format("%,d FCFA", revenue));
        
        statsLabel.setText(String.format("📊 %d abonnements chargés • %d actifs • %d expirés", 
            total, active, expired));
    }

    private void updateStatCard(String id, String value) {
        Label valueLabel = (Label) root.lookup("#" + id + "Value");
        if (valueLabel != null) {
            valueLabel.setText(value);
        }
    }

    private void resetFilters() {
        searchField.clear();
        statusFilter.getSelectionModel().select("Tous");
        planFilter.getSelectionModel().select("Tous");
        loadPage(1);
        showToast("✓ Filtres réinitialisés", "success");
    }

    private void showToast(String message, String type) {
        HBox toast = new HBox(12);
        toast.getStyleClass().add("card");
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(15, 20, 15, 20));
        toast.setMaxWidth(400);
        
        FontIcon icon;
        String color;
        
        switch(type) {
            case "success":
                icon = new FontIcon(FontAwesomeSolid.CHECK_CIRCLE);
                color = "#00f2c3";
                break;
            case "error":
                icon = new FontIcon(FontAwesomeSolid.TIMES_CIRCLE);
                color = "#ff6b9d";
                break;
            case "warning":
                icon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
                color = "#feca57";
                break;
            default:
                icon = new FontIcon(FontAwesomeSolid.INFO_CIRCLE);
                color = "#667eea";
        }
        
        icon.setIconSize(20);
        icon.setIconColor(Color.web(color));
        
        Label msg = new Label(message);
        msg.setTextFill(Color.WHITE);
        msg.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        msg.setWrapText(true);
        HBox.setHgrow(msg, Priority.ALWAYS);
        
        toast.getChildren().addAll(icon, msg);
        
        toast.setTranslateX(400);
        toast.setOpacity(0);
        
        toastContainer.getChildren().add(toast);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), toast);
        tt.setToX(0);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), toast);
        ft.setToValue(1.0);
        
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> toastContainer.getChildren().remove(toast));
            fadeOut.play();
        }));
        timeline.play();
    }

    public Node getRoot() { return root; }
}