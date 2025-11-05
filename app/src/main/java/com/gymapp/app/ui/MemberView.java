package com.gymapp.app.ui;

import com.gymapp.app.data.Member;
import com.gymapp.app.data.MemberService;
import com.gymapp.app.data.Subscription;
import com.gymapp.app.data.SubscriptionService;
import com.gymapp.app.util.QRUtil;
import com.gymapp.app.util.NetUtil;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

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
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField weightField = new TextField();
    private final TextField heightField = new TextField();
    private final TextField qrField = new TextField();
    private final ComboBox<String> genderCombo = new ComboBox<>();
    private final DatePicker birthDatePicker = new DatePicker();
    private final ComboBox<Member.SubscriptionStatus> statusCombo = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final ImageView qrPreview = new ImageView();
    private final CheckBox cbHyper = new CheckBox("Hypertension");
    private final CheckBox cbHypo = new CheckBox("Hypotension");
    private final CheckBox cbAsthma = new CheckBox("Asthme");
    private final CheckBox cbAppend = new CheckBox("Appendicite");
    private final Label statusIndicator = new Label("INACTIF");
    private final ComboBox<Integer> monthsCombo = new ComboBox<>();
    private final StackPane toastContainer = new StackPane();

    private final BorderPane root = new BorderPane();
    private final ScrollPane formScroll = new ScrollPane();

    public MemberView() {
        root.setStyle("-fx-background-color: transparent;");
        buildTable();
        buildForm();
        refresh();
    }

    private void buildTable() {
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(25));
        leftPanel.setPrefWidth(500);
        
        // === HEADER avec recherche ===
        VBox headerBox = new VBox(15);
        
        Label titleLabel = new Label("Liste des Membres");
        titleLabel.getStyleClass().add("header-title");
        
        FontIcon userIcon = new FontIcon(FontAwesomeSolid.USERS);
        userIcon.setIconSize(28);
        userIcon.setIconColor(Color.web("#667eea"));
        titleLabel.setGraphic(userIcon);
        titleLabel.setContentDisplay(ContentDisplay.LEFT);
        titleLabel.setGraphicTextGap(15);
        
        // Carte de recherche
        VBox searchCard = new VBox(15);
        searchCard.getStyleClass().add("card");
        
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon searchIcon = new FontIcon(FontAwesomeSolid.SEARCH);
        searchIcon.setIconSize(16);
        searchIcon.setIconColor(Color.web("#667eea"));
        
        searchField.setPromptText("Rechercher un membre...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button searchBtn = createIconButton("", FontAwesomeSolid.SEARCH, "btn-primary");
        searchBtn.setPrefSize(40, 40);
        
        searchBox.getChildren().addAll(searchIcon, searchField, searchBtn);
        searchCard.getChildren().add(searchBox);
        
        searchField.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.isBlank()) refresh();
            else data.setAll(service.searchByName(n));
        });
        
        headerBox.getChildren().addAll(titleLabel, searchCard);
        
        // === TABLE moderne ===
        VBox tableCard = new VBox(15);
        tableCard.getStyleClass().add("card");
        
        Label tableTitle = new Label("📋 " + data.size() + " membres");
        tableTitle.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.WHITE);
        
        TableColumn<Member, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getId() != null ? "#" + c.getValue().getId() : ""));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-font-weight: 700; -fx-text-fill: #667eea;");
        
        TableColumn<Member, String> nameCol = new TableColumn<>("👤 Nom Complet");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFullName()));
        nameCol.setPrefWidth(250);
        nameCol.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        
        TableColumn<Member, String> statusCol = new TableColumn<>("📊 Statut");
        statusCol.setCellValueFactory(c -> {
            var st = c.getValue().getSubscriptionStatus();
            return new javafx.beans.property.SimpleStringProperty(st == null ? "INACTIF" : st.name());
        });
        statusCol.setPrefWidth(130);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override 
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setText(null); 
                    setGraphic(null);
                    return; 
                }
                
                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                if ("ACTIVE".equals(item)) {
                    badge.getStyleClass().add("badge-success");
                    badge.setText("✓ ACTIF");
                } else {
                    badge.getStyleClass().add("badge-danger");
                    badge.setText("✗ INACTIF");
                }
                setGraphic(badge);
            }
        });

        table.getColumns().setAll(idCol, nameCol, statusCol);
        table.setItems(data);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> populateForm(sel));
        
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(tableTitle, table);
        
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        leftPanel.getChildren().addAll(headerBox, tableCard);
        
        root.setLeft(leftPanel);
    }

    private void buildForm() {
        VBox rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(25));
        rightPanel.setPrefWidth(600);
        
        // === HEADER du formulaire ===
        HBox formHeader = new HBox(15);
        formHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label formTitle = new Label("Profil du Membre");
        formTitle.getStyleClass().add("header-title");
        
        FontIcon profileIcon = new FontIcon(FontAwesomeSolid.USER_CIRCLE);
        profileIcon.setIconSize(28);
        profileIcon.setIconColor(Color.web("#667eea"));
        formTitle.setGraphic(profileIcon);
        formTitle.setContentDisplay(ContentDisplay.LEFT);
        formTitle.setGraphicTextGap(15);
        
        formHeader.getChildren().add(formTitle);
        
        // === SECTION Informations Personnelles ===
        VBox personalCard = createSectionCard("👤 Informations Personnelles");
        
        GridPane personalGrid = new GridPane();
        personalGrid.setHgap(15);
        personalGrid.setVgap(15);
        
        genderCombo.getItems().addAll("Homme", "Femme", "Autre");
        genderCombo.setPromptText("Sélectionnez...");
        
        int r = 0;
        personalGrid.add(createFormField("Prénom", firstNameField), 0, r);
        personalGrid.add(createFormField("Nom", lastNameField), 1, r++);
        personalGrid.add(createFormField("Nom Complet", nameField), 0, r, 2, 1); r++;
        personalGrid.add(createFormField("Genre", genderCombo), 0, r);
        personalGrid.add(createFormField("Date de Naissance", birthDatePicker), 1, r++);
        personalGrid.add(createFormField("Email", emailField), 0, r, 2, 1); r++;
        personalGrid.add(createFormField("Téléphone", phoneField), 0, r);
        personalGrid.add(createFormField("Adresse", addressField), 1, r++);
        
        personalCard.getChildren().add(personalGrid);
        
        // === SECTION Physique ===
        VBox physicalCard = createSectionCard("⚖️ Informations Physiques");
        
        HBox physicalBox = new HBox(15);
        physicalBox.getChildren().addAll(
            createFormField("Poids (kg)", weightField),
            createFormField("Taille (cm)", heightField)
        );
        
        physicalCard.getChildren().add(physicalBox);
        
        // === SECTION Médicale ===
        VBox medicalCard = createSectionCard("🏥 Antécédents Médicaux");
        
        FlowPane medicalFlow = new FlowPane(15, 15);
        medicalFlow.getChildren().addAll(cbHyper, cbHypo, cbAsthma, cbAppend);
        
        medicalCard.getChildren().add(medicalFlow);
        
        // === SECTION QR Code ===
        VBox qrCard = createSectionCard("📱 Code QR d'Identification");
        
        HBox qrInputBox = new HBox(12);
        qrField.setPromptText("Code unique du membre");
        HBox.setHgrow(qrField, Priority.ALWAYS);
        
        Button genQrBtn = createIconButton("Générer", FontAwesomeSolid.QRCODE, "btn-secondary");
        genQrBtn.setOnAction(e -> generateQr());
        
        qrInputBox.getChildren().addAll(qrField, genQrBtn);
        
        // Container QR avec effet
        StackPane qrContainer = new StackPane();
        qrContainer.getStyleClass().add("qr-container");
        qrContainer.setPrefSize(180, 180);
        qrContainer.setMaxSize(180, 180);
        
        qrPreview.setFitWidth(160);
        qrPreview.setFitHeight(160);
        qrContainer.getChildren().add(qrPreview);
        
        HBox qrDisplayBox = new HBox(qrContainer);
        qrDisplayBox.setAlignment(Pos.CENTER);
        
        qrCard.getChildren().addAll(qrInputBox, qrDisplayBox);
        
        // === SECTION Abonnement ===
        VBox subscriptionCard = createSectionCard("📋 Gestion de l'Abonnement");
        
        HBox statusBox = new HBox(15);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label statusLabel = new Label("Statut actuel:");
        statusLabel.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        statusLabel.setTextFill(Color.web("#a0aec0"));
        
        statusIndicator.getStyleClass().addAll("badge", "badge-danger");
        statusIndicator.setText("INACTIF");
        
        statusBox.getChildren().addAll(statusLabel, statusIndicator);
        
        HBox subActionsBox = new HBox(12);
        subActionsBox.setAlignment(Pos.CENTER_LEFT);
        
        monthsCombo.getItems().setAll(1, 3, 6, 12);
        monthsCombo.getSelectionModel().select(Integer.valueOf(1));
        monthsCombo.setPromptText("Durée");
        monthsCombo.setPrefWidth(100);
        
        Button subscribeBtn = createIconButton("Abonner", FontAwesomeSolid.CHECK_CIRCLE, "btn-success");
        Button renewBtn = createIconButton("Réabonner", FontAwesomeSolid.REDO, "btn-warning");
        
        subscribeBtn.setOnAction(e -> subscribe());
        renewBtn.setOnAction(e -> renew());
        
        subActionsBox.getChildren().addAll(
            new Label("Durée:"),
            monthsCombo,
            subscribeBtn,
            renewBtn
        );
        
        subscriptionCard.getChildren().addAll(statusBox, subActionsBox);
        
        // === HISTORIQUE Abonnements ===
        VBox historyCard = createSectionCard("📜 Historique des Abonnements");
        
        TableColumn<Subscription, String> planCol = new TableColumn<>("Forfait");
        planCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPlan() == null ? "" : c.getValue().getPlan()));
        planCol.setPrefWidth(120);
        
        TableColumn<Subscription, String> startCol = new TableColumn<>("Début");
        startCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartAt() == null ? "" : c.getValue().getStartAt().toString()));
        startCol.setPrefWidth(100);
        
        TableColumn<Subscription, String> endCol = new TableColumn<>("Fin");
        endCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndAt() == null ? "" : c.getValue().getEndAt().toString()));
        endCol.setPrefWidth(100);
        
        TableColumn<Subscription, String> statusSubCol = new TableColumn<>("Statut");
        statusSubCol.setCellValueFactory(c -> {
            boolean expired = c.getValue().getEndAt() != null && c.getValue().getEndAt().isBefore(LocalDate.now());
            return new javafx.beans.property.SimpleStringProperty(expired ? "Expiré" : "Actif");
        });
        statusSubCol.setPrefWidth(100);
        statusSubCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add("Actif".equals(item) ? "badge-success" : "badge-danger");
                setGraphic(badge);
            }
        });
        
        subsTable.getColumns().setAll(planCol, startCol, endCol, statusSubCol);
        subsTable.setItems(subsData);
        subsTable.setPrefHeight(200);
        
        historyCard.getChildren().add(subsTable);
        
        // === BOUTONS D'ACTION ===
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = createIconButton("💾 Enregistrer", FontAwesomeSolid.SAVE, "btn-success");
        Button newBtn = createIconButton("➕ Nouveau", FontAwesomeSolid.PLUS_CIRCLE, "btn-primary");
        Button deleteBtn = createIconButton("🗑️ Supprimer", FontAwesomeSolid.TRASH, "btn-danger");
        
        saveBtn.setPrefWidth(180);
        newBtn.setPrefWidth(160);
        deleteBtn.setPrefWidth(160);
        
        saveBtn.setOnAction(e -> save());
        newBtn.setOnAction(e -> clearForm());
        deleteBtn.setOnAction(e -> delete());
        
        actionBox.getChildren().addAll(saveBtn, newBtn, deleteBtn);
        
        // === ASSEMBLY ===
        VBox formContent = new VBox(20);
        formContent.getChildren().addAll(
            formHeader,
            personalCard,
            physicalCard,
            medicalCard,
            qrCard,
            subscriptionCard,
            historyCard,
            actionBox
        );
        
        formScroll.setContent(formContent);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        rightPanel.getChildren().add(formScroll);
        VBox.setVgrow(formScroll, Priority.ALWAYS);
        
        root.setCenter(rightPanel);
        
        // Toast container
        root.setTop(toastContainer);
        toastContainer.setAlignment(Pos.TOP_RIGHT);
        toastContainer.setPadding(new Insets(20));
        toastContainer.setPickOnBounds(false);
    }

    private VBox createSectionCard(String title) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);
        
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(102, 126, 234, 0.2);");
        
        card.getChildren().addAll(titleLabel, sep);
        return card;
    }

    private VBox createFormField(String labelText, Control input) {
        VBox field = new VBox(8);
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.web("#a0aec0"));
        
        field.getChildren().addAll(label, input);
        HBox.setHgrow(field, Priority.ALWAYS);
        
        if (input instanceof TextField) {
            ((TextField) input).setPromptText("Saisissez " + labelText.toLowerCase());
        }
        
        return field;
    }

    private Button createIconButton(String text, FontAwesomeSolid iconType, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", styleClass);
        
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(14);
        btn.setGraphic(icon);
        
        return btn;
    }

    private void refresh() {
        data.setAll(service.listAll());
    }

    private void populateForm(Member m) {
        if (m == null) { clearForm(); return; }
        
        firstNameField.setText(m.getFirstName());
        lastNameField.setText(m.getLastName());
        nameField.setText(m.getFullName());
        
        if (m.getGender() != null) {
            if ("H".equals(m.getGender())) genderCombo.getSelectionModel().select("Homme");
            else if ("F".equals(m.getGender())) genderCombo.getSelectionModel().select("Femme");
            else genderCombo.getSelectionModel().select("Autre");
        }
        
        birthDatePicker.setValue(m.getBirthDate());
        emailField.setText(m.getEmail());
        phoneField.setText(m.getPhone());
        addressField.setText(m.getAddress());
        
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
        } else {
            qrPreview.setImage(null);
        }
        
        subsData.setAll(subService.listByMember(m));
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        nameField.clear();
        genderCombo.getSelectionModel().clearSelection();
        birthDatePicker.setValue(null);
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        weightField.clear();
        heightField.clear();
        qrField.clear();
        statusCombo.getSelectionModel().select(Member.SubscriptionStatus.INACTIVE);
        cbHyper.setSelected(false);
        cbHypo.setSelected(false);
        cbAsthma.setSelected(false);
        cbAppend.setSelected(false);
        statusIndicator.setText("INACTIF");
        statusIndicator.getStyleClass().clear();
        statusIndicator.getStyleClass().addAll("badge", "badge-danger");
        qrPreview.setImage(null);
        subsData.clear();
    }

    private void save() {
        try {
            Member sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) sel = new Member();
            
            sel.setFirstName(firstNameField.getText());
            sel.setLastName(lastNameField.getText());
            sel.setFullName(nameField.getText());
            
            String gender = genderCombo.getValue();
            if ("Homme".equals(gender)) sel.setGender("H");
            else if ("Femme".equals(gender)) sel.setGender("F");
            else sel.setGender(gender);
            
            sel.setBirthDate(birthDatePicker.getValue());
            sel.setEmail(emailField.getText());
            sel.setPhone(phoneField.getText());
            sel.setAddress(addressField.getText());
            
            try { sel.setWeightKg(weightField.getText().isBlank() ? null : Double.parseDouble(weightField.getText())); } catch (Exception ignored) {}
            try { sel.setHeightCm(heightField.getText().isBlank() ? null : Double.parseDouble(heightField.getText())); } catch (Exception ignored) {}
            sel.setQrCode(qrField.getText().isBlank() ? null : qrField.getText());
            sel.setSubscriptionStatus(statusCombo.getValue());
            sel.setHypertension(cbHyper.isSelected());
            sel.setHypotension(cbHypo.isSelected());
            sel.setAsthma(cbAsthma.isSelected());
            sel.setAppendicitis(cbAppend.isSelected());

            if (sel.getId() == null) service.save(sel); else service.update(sel);
            
            showToast("✅ Membre enregistré avec succès!", "success");
            clearForm();
            refresh();
        } catch (Exception e) {
            showToast("❌ Erreur: " + e.getMessage(), "error");
        }
    }

    private void delete() {
        Member sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showToast("⚠️ Veuillez sélectionner un membre", "warning");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Êtes-vous sûr de vouloir supprimer " + sel.getFullName() + " ?", 
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation de suppression");
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                service.delete(sel);
                showToast("✅ Membre supprimé", "success");
                refresh();
                clearForm();
            }
        });
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
        showToast("✅ QR Code généré!", "success");
    }

    private void subscribe() {
        try {
            Member sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) sel = new Member();
            
            sel.setFirstName(firstNameField.getText());
            sel.setLastName(lastNameField.getText());
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
            
            showToast("✅ Abonnement créé avec succès!", "success");
            populateForm(sel);
            updateStatusIndicator(sel);
            refresh();
        } catch (Exception e) {
            showToast("❌ Erreur: " + e.getMessage(), "error");
        }
    }

    private void renew() {
        Member sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showToast("⚠️ Veuillez sélectionner un membre", "warning");
            return;
        }
        
        try {
            Integer months = monthsCombo.getValue() == null ? 1 : monthsCombo.getValue();
            subService.renew(sel, months, 0, "Renouvellement");
            
            showToast("✅ Abonnement renouvelé pour " + months + " mois!", "success");
            populateForm(sel);
            updateStatusIndicator(sel);
            refresh();
        } catch (Exception e) {
            showToast("❌ Erreur: " + e.getMessage(), "error");
        }
    }

    private void updateStatusIndicator(Member m) {
        boolean active = m.getSubscriptionStatus() == Member.SubscriptionStatus.ACTIVE;
        statusIndicator.setText(active ? "✓ ACTIF" : "✗ INACTIF");
        statusIndicator.getStyleClass().clear();
        statusIndicator.getStyleClass().addAll("badge", active ? "badge-success" : "badge-danger");
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
        
        // Position et animation
        toast.setTranslateX(400);
        toast.setOpacity(0);
        
        toastContainer.getChildren().add(toast);
        
        // Animation d'entrée
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), toast);
        tt.setToX(0);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), toast);
        ft.setToValue(1.0);
        
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
        
        // Auto-disparition après 4 secondes
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