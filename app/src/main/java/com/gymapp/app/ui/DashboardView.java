package com.gymapp.app.ui;

import com.gymapp.app.data.Attendance;
import com.gymapp.app.data.AttendanceService;
import com.gymapp.app.util.NetUtil;
import com.gymapp.app.util.QRUtil;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DashboardView {
    private final AttendanceService attendanceService = new AttendanceService();
    private final ObservableList<Attendance> latest = FXCollections.observableArrayList();

    private final VBox root = new VBox(25);
    private final ScrollPane scroll = new ScrollPane();
    private final Label urlLabel = new Label();
    private final ImageView qrImage = new ImageView();
    private final TableView<Attendance> table = new TableView<>();
    private final ComboBox<String> ipCombo = new ComboBox<>();
    private final Label timeLabel = new Label();
    private final Label statsLabel = new Label();

    public DashboardView() {
        root.setStyle("-fx-background-color: transparent;");
        build();
        refresh();
        startClock();
        scroll.setContent(root);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    }

    private void build() {
        root.setPadding(new Insets(30));

        // === HERO SECTION avec animation ===
        VBox heroSection = createHeroSection();
        
        // === STATS CARDS EN GRILLE ===
        GridPane statsGrid = createStatsGrid();
        
        // === QR CODE CARD avec effet glassmorphism ===
        VBox qrCard = createQRCard();
        
        // === RECENT CHECK-INS avec design moderne ===
        VBox checkinsCard = createCheckinsCard();

        root.getChildren().addAll(heroSection, statsGrid, qrCard, checkinsCard);
    }

    private VBox createHeroSection() {
        VBox hero = new VBox(15);
        hero.setAlignment(Pos.CENTER_LEFT);
        
        // Icône animée
        FontIcon gymIcon = new FontIcon(FontAwesomeSolid.DUMBBELL);
        gymIcon.setIconSize(48);
        gymIcon.setIconColor(Color.web("#667eea"));
        
        // Animation de rotation
        RotateTransition rotate = new RotateTransition(Duration.seconds(3), gymIcon);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
        
        // Titre avec gradient
        Label title = new Label("Tableau de Bord");
        title.getStyleClass().add("header-title");
        title.setGraphic(gymIcon);
        title.setContentDisplay(ContentDisplay.LEFT);
        title.setGraphicTextGap(20);
        
        // Horloge en temps réel
        timeLabel.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 16));
        timeLabel.setTextFill(Color.web("#a0aec0"));
        updateTime();
        
        // Stats rapides
        statsLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        statsLabel.setTextFill(Color.web("#718096"));
        updateStats();
        
        hero.getChildren().addAll(title, timeLabel, statsLabel);
        return hero;
    }

    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        
        // 4 cartes de statistiques
        VBox card1 = createStatCard("👥", "Membres Actifs", "247", "#00f2c3");
        VBox card2 = createStatCard("📊", "Check-ins Aujourd'hui", "83", "#667eea");
        VBox card3 = createStatCard("💰", "Revenus du Mois", "1.2M FCFA", "#feca57");
        VBox card4 = createStatCard("⚡", "Taux de Fréquentation", "78%", "#ff6b9d");
        
        grid.add(card1, 0, 0);
        grid.add(card2, 1, 0);
        grid.add(card3, 0, 1);
        grid.add(card4, 1, 1);
        
        // Rendre les colonnes égales
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);
        
        return grid;
    }

    private VBox createStatCard(String emoji, String label, String value, String accentColor) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(140);
        
        // Effet de bordure colorée
        card.setStyle(card.getStyle() + "-fx-border-color: " + accentColor + "80; -fx-border-width: 0 0 0 4;");
        
        // Icône
        Label icon = new Label(emoji);
        icon.setFont(Font.font(32));
        
        // Valeur principale
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Inter", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(accentColor));
        
        // Label descriptif
        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
        descLabel.setTextFill(Color.web("#a0aec0"));
        
        // Animation au survol
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        card.getChildren().addAll(icon, valueLabel, descLabel);
        return card;
    }

    private VBox createQRCard() {
        VBox qrCard = new VBox(20);
        qrCard.getStyleClass().add("card");
        qrCard.setAlignment(Pos.CENTER);
        
        // Titre avec icône
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER);
        
        FontIcon qrIcon = new FontIcon(FontAwesomeSolid.QRCODE);
        qrIcon.setIconSize(24);
        qrIcon.setIconColor(Color.web("#667eea"));
        
        Label qrTitle = new Label("QR Code de Check-In");
        qrTitle.getStyleClass().add("subheader");
        qrTitle.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        
        titleBox.getChildren().addAll(qrIcon, qrTitle);
        
        // Sélecteur IP avec design moderne
        HBox ipSelector = new HBox(12);
        ipSelector.setAlignment(Pos.CENTER);
        
        FontIcon networkIcon = new FontIcon(FontAwesomeSolid.NETWORK_WIRED);
        networkIcon.setIconSize(16);
        networkIcon.setIconColor(Color.web("#a0aec0"));
        
        Label ipLabel = new Label("Adresse IP:");
        ipLabel.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        ipLabel.setTextFill(Color.web("#a0aec0"));
        ipLabel.setGraphic(networkIcon);
        ipLabel.setContentDisplay(ContentDisplay.LEFT);
        
        ipCombo.setEditable(false);
        ipCombo.setPrefWidth(200);
        ipCombo.getItems().setAll(new ArrayList<>(NetUtil.listLocalIps()));
        if (!ipCombo.getItems().isEmpty()) ipCombo.getSelectionModel().select(0);
        
        Button refreshIps = new Button("Rafraîchir");
        refreshIps.getStyleClass().addAll("button", "btn-secondary");
        
        FontIcon refreshIcon = new FontIcon(FontAwesomeSolid.SYNC_ALT);
        refreshIcon.setIconSize(14);
        refreshIcon.setIconColor(Color.web("#a0aec0"));
        refreshIps.setGraphic(refreshIcon);
        
        refreshIps.setOnAction(e -> {
            // Animation de rotation sur l'icône
            RotateTransition rt = new RotateTransition(Duration.millis(500), refreshIcon);
            rt.setByAngle(360);
            rt.play();
            
            var sel = ipCombo.getValue();
            ipCombo.getItems().setAll(new ArrayList<>(NetUtil.listLocalIps()));
            if (sel != null && ipCombo.getItems().contains(sel)) ipCombo.getSelectionModel().select(sel);
            else if (!ipCombo.getItems().isEmpty()) ipCombo.getSelectionModel().select(0);
            updateUrlAndQr();
        });
        
        ipCombo.setOnAction(e -> updateUrlAndQr());
        ipSelector.getChildren().addAll(ipLabel, ipCombo, refreshIps);
        
        // Container QR avec effet glassmorphism
        StackPane qrContainer = new StackPane();
        qrContainer.getStyleClass().add("qr-container");
        qrContainer.setPrefSize(250, 250);
        
        // Cercles décoratifs animés en arrière-plan
        Circle circle1 = new Circle(60);
        circle1.setFill(Color.web("#667eea20"));
        circle1.setTranslateX(-80);
        circle1.setTranslateY(-80);
        
        Circle circle2 = new Circle(50);
        circle2.setFill(Color.web("#764ba220"));
        circle2.setTranslateX(80);
        circle2.setTranslateY(80);
        
        // Animation des cercles
        ScaleTransition st1 = new ScaleTransition(Duration.seconds(3), circle1);
        st1.setFromX(1.0);
        st1.setFromY(1.0);
        st1.setToX(1.2);
        st1.setToY(1.2);
        st1.setCycleCount(Animation.INDEFINITE);
        st1.setAutoReverse(true);
        st1.play();
        
        ScaleTransition st2 = new ScaleTransition(Duration.seconds(2.5), circle2);
        st2.setFromX(1.0);
        st2.setFromY(1.0);
        st2.setToX(1.3);
        st2.setToY(1.3);
        st2.setCycleCount(Animation.INDEFINITE);
        st2.setAutoReverse(true);
        st2.play();
        
        qrImage.setFitWidth(220);
        qrImage.setFitHeight(220);
        
        qrContainer.getChildren().addAll(circle1, circle2, qrImage);
        
        // URL avec icône de copie
        HBox urlBox = new HBox(10);
        urlBox.setAlignment(Pos.CENTER);
        
        FontIcon linkIcon = new FontIcon(FontAwesomeSolid.LINK);
        linkIcon.setIconSize(14);
        linkIcon.setIconColor(Color.web("#667eea"));
        
        urlLabel.getStyleClass().add("subheader");
        urlLabel.setGraphic(linkIcon);
        urlLabel.setContentDisplay(ContentDisplay.LEFT);
        
        Button copyBtn = new Button("Copier");
        copyBtn.getStyleClass().addAll("button", "btn-secondary");
        copyBtn.setPrefWidth(80);
        
        FontIcon copyIcon = new FontIcon(FontAwesomeSolid.COPY);
        copyIcon.setIconSize(12);
        copyBtn.setGraphic(copyIcon);
        
        copyBtn.setOnAction(e -> {
            // Copier dans le presse-papier
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(urlLabel.getText().replace("🔗 ", ""));
            clipboard.setContent(content);
            
            // Animation de confirmation
            copyBtn.setText("✓ Copié!");
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), ev -> copyBtn.setText("Copier")));
            timeline.play();
        });
        
        urlBox.getChildren().addAll(urlLabel, copyBtn);
        
        // Instruction avec pulse animation
        Label instructionLabel = new Label("📲 Scannez ce QR code pour accéder au check-in mobile");
        instructionLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
        instructionLabel.setTextFill(Color.web("#718096"));
        instructionLabel.setWrapText(true);
        instructionLabel.setMaxWidth(400);
        instructionLabel.setAlignment(Pos.CENTER);
        
        qrCard.getChildren().addAll(titleBox, ipSelector, qrContainer, urlBox, instructionLabel);
        updateUrlAndQr();
        
        return qrCard;
    }

    private VBox createCheckinsCard() {
        VBox checkinsCard = new VBox(20);
        checkinsCard.getStyleClass().add("card");
        
        // Titre avec icône
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon clockIcon = new FontIcon(FontAwesomeSolid.CLOCK);
        clockIcon.setIconSize(22);
        clockIcon.setIconColor(Color.web("#667eea"));
        
        Label checkinsTitle = new Label("Derniers Check-Ins");
        checkinsTitle.getStyleClass().add("subheader");
        checkinsTitle.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        checkinsTitle.setGraphic(clockIcon);
        checkinsTitle.setContentDisplay(ContentDisplay.LEFT);
        
        titleBox.getChildren().add(checkinsTitle);
        
        // Table avec icônes
        TableColumn<Attendance, String> nameCol = new TableColumn<>("👤 Membre");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getMember() != null ? c.getValue().getMember().getFullName() : "?"));
        nameCol.setPrefWidth(300);
        nameCol.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        
        TableColumn<Attendance, String> timeCol = new TableColumn<>("🕐 Heure");
        timeCol.setCellValueFactory(c -> {
            if (c.getValue().getCheckedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                return new javafx.beans.property.SimpleStringProperty(
                    c.getValue().getCheckedAt().format(formatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        timeCol.setPrefWidth(150);
        timeCol.setStyle("-fx-font-size: 14px;");
        
        // Colonne de statut avec badge
        TableColumn<Attendance, String> statusCol = new TableColumn<>("📊 Statut");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("✓ Vérifié"));
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #00f2c3; -fx-font-weight: 700;");
                }
            }
        });

        table.getColumns().setAll(nameCol, timeCol, statusCol);
        table.setItems(latest);
        table.setPlaceholder(new Label("Aucun check-in récent"));
        
        // Bouton de rafraîchissement avec animation
        Button refreshBtn = new Button("Actualiser");
        refreshBtn.getStyleClass().addAll("button", "btn-success");
        
        FontIcon refreshIcon = new FontIcon(FontAwesomeSolid.SYNC_ALT);
        refreshIcon.setIconSize(14);
        refreshIcon.setIconColor(Color.web("#0f0f23"));
        refreshBtn.setGraphic(refreshIcon);
        
        refreshBtn.setOnAction(e -> {
            RotateTransition rt = new RotateTransition(Duration.millis(500), refreshIcon);
            rt.setByAngle(360);
            rt.play();
            refresh();
        });
        
        HBox refreshBox = new HBox(refreshBtn);
        refreshBox.setAlignment(Pos.CENTER_RIGHT);

        VBox.setVgrow(table, Priority.ALWAYS);
        checkinsCard.getChildren().addAll(titleBox, table, refreshBox);
        
        return checkinsCard;
    }

    private void refresh() {
        latest.setAll(attendanceService.latest(20));
        updateStats();
    }

    private void updateUrlAndQr() {
        String ip = ipCombo.getValue() == null ? NetUtil.findLocalIp() : ipCombo.getValue();
        String url = "http://" + ip + ":8080/";
        urlLabel.setText("🔗 " + url);
        var png = QRUtil.generateQrPng(url, 220);
        qrImage.setImage(QRUtil.imageFromBytes(png));
    }

    private void startClock() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy • HH:mm:ss");
        timeLabel.setText("⏰ " + LocalDateTime.now().format(formatter));
    }

    private void updateStats() {
        int todayCheckins = latest.size();
        statsLabel.setText(String.format("📊 %d check-ins enregistrés aujourd'hui", todayCheckins));
    }

    public Node getRoot() { return scroll; }
}