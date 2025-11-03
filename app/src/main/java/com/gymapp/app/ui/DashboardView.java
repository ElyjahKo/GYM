package com.gymapp.app.ui;

import com.gymapp.app.data.Attendance;
import com.gymapp.app.data.AttendanceService;
import com.gymapp.app.util.NetUtil;
import com.gymapp.app.util.QRUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;

public class DashboardView {
    private final AttendanceService attendanceService = new AttendanceService();
    private final ObservableList<Attendance> latest = FXCollections.observableArrayList();

    private final VBox root = new VBox(20);
    private final ScrollPane scroll = new ScrollPane();
    private final Label urlLabel = new Label();
    private final ImageView qrImage = new ImageView();
    private final TableView<Attendance> table = new TableView<>();
    private final ComboBox<String> ipCombo = new ComboBox<>();

    public DashboardView() {
        root.setStyle("-fx-background-color: #f5f5f5;");
        build();
        refresh();
        scroll.setContent(root);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: #f5f5f5; -fx-background-color: #f5f5f5;");
    }

    private void build() {
        root.setPadding(new Insets(20));

        // Welcome header
        Label welcomeLabel = new Label("🏋️ Tableau de Bord");
        welcomeLabel.getStyleClass().add("header-title");

        // QR Code Card
        VBox qrCard = new VBox(15);
        qrCard.getStyleClass().add("card");
        qrCard.setSpacing(15);
        qrCard.setPadding(new Insets(25));
        qrCard.setAlignment(Pos.CENTER);

        Label qrTitle = new Label("📱 QR Code de Check-In");
        qrTitle.getStyleClass().add("subheader");

        // IP selector with modern styling
        HBox ipSelector = new HBox(10);
        ipSelector.setAlignment(Pos.CENTER);
        Label ipLabel = new Label("🌐 Adresse IP:");
        ipLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        
        ipCombo.setEditable(false);
        ipCombo.setPrefWidth(180);
        ipCombo.setStyle("-fx-background-radius: 8; -fx-padding: 8 15; -fx-font-size: 13px;");
        ipCombo.getItems().setAll(new ArrayList<>(NetUtil.listLocalIps()));
        if (!ipCombo.getItems().isEmpty()) ipCombo.getSelectionModel().select(0);

        Button refreshIps = new Button("🔄 Rafraîchir");
        refreshIps.getStyleClass().addAll("button", "btn-secondary");
        
        refreshIps.setOnAction(e -> {
            var sel = ipCombo.getValue();
            ipCombo.getItems().setAll(new ArrayList<>(NetUtil.listLocalIps()));
            if (sel != null && ipCombo.getItems().contains(sel)) ipCombo.getSelectionModel().select(sel);
            else if (!ipCombo.getItems().isEmpty()) ipCombo.getSelectionModel().select(0);
            updateUrlAndQr();
        });
        ipCombo.setOnAction(e -> updateUrlAndQr());

        ipSelector.getChildren().addAll(ipLabel, ipCombo, refreshIps);

        // QR Image with border
        VBox qrImageBox = new VBox();
        qrImageBox.setAlignment(Pos.CENTER);
        qrImageBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 10;");
        qrImage.setFitWidth(220);
        qrImage.setFitHeight(220);
        qrImageBox.getChildren().add(qrImage);

        // URL Label with copy-friendly style
        urlLabel.getStyleClass().add("subheader");

        Label instructionLabel = new Label("📲 Scannez ce QR code pour accéder au check-in");
        instructionLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        instructionLabel.setTextFill(Color.web("#6c757d"));

        qrCard.getChildren().addAll(qrTitle, ipSelector, qrImageBox, urlLabel, instructionLabel);
        
        updateUrlAndQr();

        // Recent Check-ins Card
        VBox checkinsCard = new VBox(15);
        checkinsCard.getStyleClass().add("card");
        checkinsCard.setSpacing(15);
        checkinsCard.setPadding(new Insets(25));

        Label checkinsTitle = new Label("🕐 Derniers Check-Ins");
        checkinsTitle.getStyleClass().add("subheader");

        // Styled table
        TableColumn<Attendance, String> nameCol = new TableColumn<>("👤 Membre");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getMember() != null ? c.getValue().getMember().getFullName() : "?"));
        nameCol.setPrefWidth(250);
        nameCol.setStyle("-fx-font-size: 13px;");
        
        TableColumn<Attendance, String> timeCol = new TableColumn<>("🕐 Heure");
        timeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCheckedAt() != null ? c.getValue().getCheckedAt().toString() : ""));
        timeCol.setPrefWidth(200);
        timeCol.setStyle("-fx-font-size: 13px;");

        table.getColumns().setAll(nameCol, timeCol);
        table.setItems(latest);
        table.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        table.setPlaceholder(new Label("Aucun check-in récent"));
        
        // Auto-refresh button
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.getStyleClass().addAll("button", "btn-success");
        refreshBtn.setOnAction(e -> refresh());
        
        HBox refreshBox = new HBox(refreshBtn);
        refreshBox.setAlignment(Pos.CENTER_RIGHT);

        VBox.setVgrow(table, Priority.ALWAYS);
        checkinsCard.getChildren().addAll(checkinsTitle, table, refreshBox);

        root.getChildren().addAll(welcomeLabel, qrCard, checkinsCard);
    }

    private void refresh() {
        latest.setAll(attendanceService.latest(20));
    }

    private void updateUrlAndQr() {
        String ip = ipCombo.getValue() == null ? NetUtil.findLocalIp() : ipCombo.getValue();
        String url = "http://" + ip + ":8080/";
        urlLabel.setText("🔗 " + url);
        var png = QRUtil.generateQrPng(url, 220);
        qrImage.setImage(QRUtil.imageFromBytes(png));
    }

    public Node getRoot() { return scroll; }
}