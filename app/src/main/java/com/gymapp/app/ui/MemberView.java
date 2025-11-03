package com.gymapp.app.ui;

import com.gymapp.app.data.Member;
import com.gymapp.app.data.MemberService;
import com.gymapp.app.data.Subscription;
import com.gymapp.app.data.SubscriptionService;
import com.gymapp.app.util.QRUtil;
import com.gymapp.app.util.NetUtil;
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
import javafx.scene.text.Text;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class MemberView {
    private final MemberService service = new MemberService();
    private final SubscriptionService subService = new SubscriptionService();
    private final ObservableList<Member> data = FXCollections.observableArrayList();
    private final ObservableList<Subscription> subsData = FXCollections.observableArrayList();

    private final TableView<Member> table = new TableView<>();
    private final TableView<Subscription> subsTable = new TableView<>();
    private final TextField nameField = new TextField();
    private final TextField weightField = new TextField();
    private final TextField heightField = new TextField();
    private final TextField qrField = new TextField();
    private final ComboBox<Member.SubscriptionStatus> statusCombo = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final ImageView qrPreview = new ImageView();
    private final CheckBox cbHyper = new CheckBox("Hypertension");
    private final CheckBox cbHypo = new CheckBox("Hypotension");
    private final CheckBox cbAsthma = new CheckBox("Asthme");
    private final CheckBox cbAppend = new CheckBox("Appendicite");
    private final Label statusIndicator = new Label("INACTIF");
    private final ComboBox<Integer> monthsCombo = new ComboBox<>();

    private final BorderPane root = new BorderPane();

    public MemberView() {
        root.setStyle("-fx-background-color: #f5f5f5;");
        buildTable();
        buildForm();
        refresh();
    }

    private void buildTable() {
        // Styled search bar
        searchField.setPromptText("🔍 Rechercher un membre...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 20; -fx-padding: 8 15; -fx-font-size: 13px;");
        
        Label titleLabel = new Label("📋 Liste des Membres");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        VBox searchContainer = new VBox(12);
        searchContainer.setPadding(new Insets(15));
        searchContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        searchContainer.getChildren().addAll(titleLabel, searchField);
        
        searchField.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.isBlank()) refresh();
            else data.setAll(service.searchByName(n));
        });

        // Styled table
        TableColumn<Member, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        idCol.setPrefWidth(60);
        
        TableColumn<Member, String> nameCol = new TableColumn<>("Nom Complet");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFullName()));
        nameCol.setPrefWidth(200);
        
        TableColumn<Member, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(c -> {
            var st = c.getValue().getSubscriptionStatus();
            return new javafx.beans.property.SimpleStringProperty(st == null ? "INACTIF" : st.name());
        });
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override 
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setText(null); 
                    setStyle(""); 
                    return; 
                }
                setText(item);
                if ("ACTIVE".equals(item)) {
                    setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-alignment: center;");
                } else {
                    setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold; -fx-alignment: center;");
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, statusCol);
        table.setItems(data);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> populateForm(sel));

        Label listTitle = new Label("📋 Liste des Membres");
        listTitle.getStyleClass().add("subheader");
        VBox left = new VBox(15, listTitle, searchContainer, table);
        left.setPadding(new Insets(15));
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setLeft(left);
    }

    private void buildForm() {
        // Header
        Label formTitle = new Label("👤 Informations du Membre");
        formTitle.getStyleClass().add("header-title");
        
        // Form container with card style
        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(20));
        formCard.getStyleClass().add("card");
        
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);

        // Style inputs
        styleTextField(nameField, "Nom complet du membre");
        styleTextField(weightField, "Ex: 70.5");
        styleTextField(heightField, "Ex: 175");
        styleTextField(qrField, "Code QR unique");
        
        statusCombo.getItems().setAll(Member.SubscriptionStatus.values());
        statusCombo.getSelectionModel().select(Member.SubscriptionStatus.INACTIVE);
        statusCombo.setStyle("-fx-background-radius: 5; -fx-padding: 8;");

        // Styled buttons
        Button saveBtn = new Button("💾 Enregistrer");
        saveBtn.getStyleClass().addAll("button", "btn-success");
        Button newBtn = new Button("➕ Nouveau");
        newBtn.getStyleClass().addAll("button", "btn-primary");
        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.getStyleClass().addAll("button", "btn-danger");
        Button genQrBtn = new Button("📱 Générer QR");
        genQrBtn.getStyleClass().addAll("button", "btn-secondary");
        Button subscribeBtn = new Button("✓ Abonner");
        subscribeBtn.getStyleClass().addAll("button", "btn-success");
        Button renewBtn = new Button("🔄 Réabonner");
        renewBtn.getStyleClass().addAll("button", "btn-warning");
        
        monthsCombo.getItems().setAll(1, 3, 6, 12);
        monthsCombo.getSelectionModel().select(Integer.valueOf(1));
        monthsCombo.setStyle("-fx-background-radius: 5;");

        int r = 0;
        form.addRow(r++, createLabel("📝 Nom:"), nameField);
        form.addRow(r++, createLabel("⚖️ Poids (kg):"), weightField);
        form.addRow(r++, createLabel("📏 Taille (cm):"), heightField);
        form.addRow(r++, createLabel("🔲 Code QR:"), qrField);
        form.addRow(r++, createLabel("📊 Statut:"), statusCombo);
        
        // Medical history with better styling
        Label medLabel = createLabel("🏥 Antécédents médicaux:");
        HBox med = new HBox(15, cbHyper, cbHypo, cbAsthma, cbAppend);
        form.add(medLabel, 0, r);
        form.add(med, 1, r++);
        
        // Status indicator with badge style (CSS classes)
        statusIndicator.getStyleClass().addAll("badge", "badge-danger");
        HBox statusBox = new HBox(statusIndicator);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        form.add(statusBox, 1, r++);
        
        // Action buttons
        HBox actions = new HBox(10, saveBtn, newBtn, deleteBtn, genQrBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        form.add(actions, 0, r, 2, 1);
        r++;
        
        // Subscription quick actions
        HBox subActions = new HBox(10, createLabel("📅 Durée:"), monthsCombo, subscribeBtn, renewBtn);
        subActions.setAlignment(Pos.CENTER_LEFT);
        form.add(subActions, 0, r, 2, 1);
        r++;
        
        // QR Preview with card
        VBox qrCard = new VBox(10);
        qrCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8; -fx-alignment: center;");
        Label qrLabel = new Label("📱 Aperçu du QR Code");
        qrLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        qrPreview.setFitWidth(160); 
        qrPreview.setFitHeight(160);
        qrCard.getChildren().addAll(qrLabel, qrPreview);
        form.add(qrCard, 0, r, 2, 1);

        formCard.getChildren().addAll(formTitle, form);

        // Button actions
        saveBtn.setOnAction(e -> save());
        newBtn.setOnAction(e -> clearForm());
        deleteBtn.setOnAction(e -> delete());
        genQrBtn.setOnAction(e -> generateQr());
        subscribeBtn.setOnAction(e -> subscribe());
        renewBtn.setOnAction(e -> renew());

        // Subscription history with title
        Label historyTitle = new Label("📜 Historique des Abonnements");
        historyTitle.getStyleClass().add("subheader");
        
        TableColumn<Subscription, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getType() == null ? "" : c.getValue().getType()));
        TableColumn<Subscription, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatus() == null ? "" : c.getValue().getStatus().name()));
        TableColumn<Subscription, String> startCol = new TableColumn<>("Début");
        startCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartAt() == null ? "" : String.valueOf(c.getValue().getStartAt())));
        TableColumn<Subscription, String> endCol = new TableColumn<>("Fin");
        endCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndAt() == null ? "" : String.valueOf(c.getValue().getEndAt())));
        subsTable.getColumns().setAll(typeCol, statusCol, startCol, endCol);
        subsTable.setItems(subsData);
        subsTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        VBox center = new VBox(15, formCard, historyTitle, subsTable);
        center.setPadding(new Insets(15));
        VBox.setVgrow(subsTable, Priority.ALWAYS);
        root.setCenter(center);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        return label;
    }

    private void styleTextField(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setStyle("-fx-background-radius: 5; -fx-padding: 8; -fx-border-color: #ddd; -fx-border-radius: 5;");
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 10 20; " +
            "-fx-background-radius: 5; -fx-font-weight: bold; -fx-cursor: hand;", color));
        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
            "-fx-background-color: derive(%s, -10%%); -fx-text-fill: white; -fx-padding: 10 20; " +
            "-fx-background-radius: 5; -fx-font-weight: bold; -fx-cursor: hand;", color)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 10 20; " +
            "-fx-background-radius: 5; -fx-font-weight: bold; -fx-cursor: hand;", color)));
        return btn;
    }

    private void refresh() {
        data.setAll(service.listAll());
    }

    private void populateForm(Member m) {
        if (m == null) { clearForm(); return; }
        nameField.setText(m.getFullName());
        weightField.setText(m.getWeightKg() == null ? "" : String.valueOf(m.getWeightKg()));
        heightField.setText(m.getHeightCm() == null ? "" : String.valueOf(m.getHeightCm()));
        qrField.setText(m.getQrCode());
        statusCombo.getSelectionModel().select(m.getSubscriptionStatus());
        cbHyper.setSelected(m.isHypertension());
        cbHypo.setSelected(m.isHypotension());
        cbAsthma.setSelected(m.isAsthma());
        cbAppend.setSelected(m.isAppendicitis());
        updateStatusIndicator(m);
        if (m.getQrCode() != null) {
            String ip = NetUtil.findLocalIp();
            String enc = URLEncoder.encode(m.getQrCode(), StandardCharsets.UTF_8);
            String link = "http://" + ip + ":8080/checkin?code=" + enc;
            var bytes = QRUtil.generateQrPng(link, 160);
            qrPreview.setImage(QRUtil.imageFromBytes(bytes));
        } else qrPreview.setImage(null);
        subsData.setAll(subService.listByMember(m));
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        nameField.clear(); weightField.clear(); heightField.clear(); qrField.clear();
        statusCombo.getSelectionModel().select(Member.SubscriptionStatus.INACTIVE);
        cbHyper.setSelected(false); cbHypo.setSelected(false); cbAsthma.setSelected(false); cbAppend.setSelected(false);
        statusIndicator.setText("INACTIF");
        statusIndicator.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20;");
        qrPreview.setImage(null);
        subsData.clear();
    }

    private void save() {
        Member sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) sel = new Member();
        sel.setFullName(nameField.getText());
        try { sel.setWeightKg(weightField.getText().isBlank() ? null : Double.parseDouble(weightField.getText())); } catch (Exception ignored) {}
        try { sel.setHeightCm(heightField.getText().isBlank() ? null : Double.parseDouble(heightField.getText())); } catch (Exception ignored) {}
        sel.setQrCode(qrField.getText().isBlank() ? null : qrField.getText());
        sel.setSubscriptionStatus(statusCombo.getValue());
        sel.setHypertension(cbHyper.isSelected());
        sel.setHypotension(cbHypo.isSelected());
        sel.setAsthma(cbAsthma.isSelected());
        sel.setAppendicitis(cbAppend.isSelected());

        if (sel.getId() == null) service.save(sel); else service.update(sel);
        clearForm(); refresh();
    }

    private void delete() {
        Member sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce membre ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(b -> { if (b == ButtonType.YES) { service.delete(sel); refresh(); clearForm(); }});
    }

    private void generateQr() {
        if (qrField.getText() == null || qrField.getText().isBlank()) {
            qrField.setText("M-" + System.currentTimeMillis());
        }
        String ip = NetUtil.findLocalIp();
        String enc = URLEncoder.encode(qrField.getText(), StandardCharsets.UTF_8);
        String link = "http://" + ip + ":8080/checkin?code=" + enc;
        var bytes = QRUtil.generateQrPng(link, 160);
        qrPreview.setImage(QRUtil.imageFromBytes(bytes));
    }

    private void subscribe() {
        Member sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) sel = new Member();
        sel.setFullName(nameField.getText());
        sel.setQrCode(qrField.getText().isBlank() ? null : qrField.getText());
        try { sel.setWeightKg(weightField.getText().isBlank() ? null : Double.parseDouble(weightField.getText())); } catch (Exception ignored) {}
        try { sel.setHeightCm(heightField.getText().isBlank() ? null : Double.parseDouble(heightField.getText())); } catch (Exception ignored) {}
        sel.setHypertension(cbHyper.isSelected());
        sel.setHypotension(cbHypo.isSelected());
        sel.setAsthma(cbAsthma.isSelected());
        sel.setAppendicitis(cbAppend.isSelected());
        Integer months = monthsCombo.getValue() == null ? 1 : monthsCombo.getValue();
        if (sel.getId() == null) sel = service.save(sel);
        subService.createInitial(sel, "Mensuel", months, 0, LocalDate.now());
        populateForm(sel);
        updateStatusIndicator(sel);
        refresh();
    }

    private void renew() {
        Member sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Integer months = monthsCombo.getValue() == null ? 1 : monthsCombo.getValue();
        subService.renew(sel, months, 0, "Renouvellement");
        populateForm(sel);
        updateStatusIndicator(sel);
        refresh();
    }

    private void updateStatusIndicator(Member m) {
        boolean active = m.getSubscriptionStatus() == Member.SubscriptionStatus.ACTIVE;
        statusIndicator.setText(active ? "ACTIF" : "INACTIF");
        String color = active ? "#28a745" : "#dc3545";
        statusIndicator.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20;", color));
    }

    public Node getRoot() { return root; }
}