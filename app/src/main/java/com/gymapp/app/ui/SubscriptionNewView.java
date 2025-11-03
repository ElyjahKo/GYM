package com.gymapp.app.ui;

import com.gymapp.app.data.Member;
import com.gymapp.app.data.MemberService;
import com.gymapp.app.data.SubscriptionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;

public class SubscriptionNewView {
    private final MemberService memberService = new MemberService();
    private final SubscriptionService subService = new SubscriptionService();

    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox root = new VBox(20);

    // Member fields
    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final ComboBox<String> gender = new ComboBox<>();
    private final DatePicker birthDate = new DatePicker();
    private final TextField email = new TextField();
    private final TextField phone = new TextField();
    private final TextField address = new TextField();

    // Subscription fields
    private final ComboBox<String> plan = new ComboBox<>();
    private final ComboBox<Integer> quantity = new ComboBox<>();
    private final DatePicker startAt = new DatePicker(LocalDate.now());

    private final Label successLabel = new Label();

    public SubscriptionNewView() {
        root.setStyle("-fx-background-color: #f5f5f5;");
        build();
        
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f5f5; -fx-background-color: #f5f5f5;");
    }

    private void build() {
        root.setPadding(new Insets(20));
        
        // Header
        Label titleLabel = new Label("➕ Nouvel Abonnement");
        titleLabel.getStyleClass().add("header-title");
        
        Label subtitleLabel = new Label("Créer un nouveau client et son abonnement");
        subtitleLabel.getStyleClass().add("subheader");

        // Member Info Card
        VBox memberCard = new VBox(15);
        memberCard.getStyleClass().add("card");
        
        Label memberTitle = new Label("👤 Informations du Client");
        memberTitle.getStyleClass().add("subheader");
        
        GridPane memberForm = new GridPane();
        memberForm.setHgap(15);
        memberForm.setVgap(15);

        gender.getItems().addAll("Homme", "Femme");
        gender.setStyle("-fx-background-radius: 8; -fx-padding: 10;");
        
        styleTextField(firstName, "Prénom du client");
        styleTextField(lastName, "Nom du client");
        styleTextField(email, "exemple@email.com");
        styleTextField(phone, "+225 XX XX XX XX XX");
        styleTextField(address, "Adresse complète");
        
        birthDate.setStyle("-fx-background-radius: 8;");

        int r = 0;
        memberForm.addRow(r++, createLabel("📝 Prénom:"), firstName);
        memberForm.addRow(r++, createLabel("📝 Nom:"), lastName);
        memberForm.addRow(r++, createLabel("⚧ Genre:"), gender);
        memberForm.addRow(r++, createLabel("🎂 Date de naissance:"), birthDate);
        memberForm.addRow(r++, createLabel("📧 Email:"), email);
        memberForm.addRow(r++, createLabel("📱 Téléphone:"), phone);
        memberForm.addRow(r++, createLabel("🏠 Adresse:"), address);

        memberCard.getChildren().addAll(memberTitle, memberForm);

        // Subscription Info Card
        VBox subscriptionCard = new VBox(15);
        subscriptionCard.getStyleClass().add("card");
        
        Label subscriptionTitle = new Label("📦 Détails de l'Abonnement");
        subscriptionTitle.getStyleClass().add("subheader");
        
        GridPane subscriptionForm = new GridPane();
        subscriptionForm.setHgap(15);
        subscriptionForm.setVgap(15);

        plan.getItems().addAll("Mensuel", "Trimestriel", "Annuel");
        plan.setStyle("-fx-background-radius: 8; -fx-padding: 10;");
        
        quantity.getItems().addAll(1, 3, 6, 12);
        quantity.getSelectionModel().select(Integer.valueOf(1));
        quantity.setStyle("-fx-background-radius: 8; -fx-padding: 10;");
        
        startAt.setStyle("-fx-background-radius: 8;");

        int s = 0;
        subscriptionForm.addRow(s++, createLabel("📋 Type de forfait:"), plan);
        subscriptionForm.addRow(s++, createLabel("🔢 Durée (mois):"), quantity);
        subscriptionForm.addRow(s++, createLabel("📅 Date de début:"), startAt);

        subscriptionCard.getChildren().addAll(subscriptionTitle, subscriptionForm);

        // Action Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button addBtn = createStyledButton("✅ Créer l'Abonnement", "#28a745");
        addBtn.getStyleClass().addAll("button", "btn-success");
        Button clearBtn = createStyledButton("🔄 Réinitialiser", "#6c757d");
        clearBtn.getStyleClass().addAll("button", "btn-secondary");
        
        addBtn.setOnAction(e -> onAdd());
        clearBtn.setOnAction(e -> clear());
        
        buttonBox.getChildren().addAll(addBtn, clearBtn);

        // Success message styling
        successLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        successLabel.setStyle("-fx-padding: 15; -fx-background-radius: 8;");
        successLabel.setAlignment(Pos.CENTER);
        successLabel.setMaxWidth(Double.MAX_VALUE);
        successLabel.setVisible(false);

        root.getChildren().addAll(titleLabel, subtitleLabel, memberCard, subscriptionCard, buttonBox, successLabel);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        label.setTextFill(Color.web("#495057"));
        return label;
    }

    private void styleTextField(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #ced4da; " +
                      "-fx-border-radius: 8; -fx-font-size: 13px;");
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                field.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #007bff; " +
                             "-fx-border-width: 2; -fx-border-radius: 8; -fx-font-size: 13px;");
            } else {
                field.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #ced4da; " +
                             "-fx-border-radius: 8; -fx-font-size: 13px;");
            }
        });
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 12 30; " +
            "-fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;", color));
        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
            "-fx-background-color: derive(%s, -15%%); -fx-text-fill: white; -fx-padding: 12 30; " +
            "-fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;", color)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 12 30; " +
            "-fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;", color)));
        return btn;
    }

    private void onAdd() {
        try {
            // Validation
            if (firstName.getText().isBlank() || lastName.getText().isBlank()) {
                showMessage("⚠️ Veuillez saisir le prénom et le nom", "#ffc107");
                return;
            }
            if (gender.getValue() == null) {
                showMessage("⚠️ Veuillez choisir le genre", "#ffc107");
                return;
            }
            if (plan.getValue() == null || quantity.getValue() == null || startAt.getValue() == null) {
                showMessage("⚠️ Veuillez renseigner le forfait, la durée et la date de début", "#ffc107");
                return;
            }

            Member m = new Member();
            m.setFirstName(firstName.getText().trim());
            m.setLastName(lastName.getText().trim());
            m.setFullName((firstName.getText().trim() + " " + lastName.getText().trim()).trim());
            m.setGender("Homme".equals(gender.getValue()) ? "H" : "F");
            m.setBirthDate(birthDate.getValue());
            m.setEmail(email.getText().trim());
            m.setPhone(phone.getText().trim());
            m.setAddress(address.getText().trim());
            m.setSubscriptionStatus(Member.SubscriptionStatus.INACTIVE);

            m = memberService.save(m);
            subService.createInitial(m, plan.getValue(), quantity.getValue(), 0, startAt.getValue());

            showMessage("✅ Client enregistré et abonnement créé avec succès!", "#28a745");
            clear();
        } catch (Exception ex) {
            showMessage("❌ Erreur: " + ex.getMessage(), "#dc3545");
        }
    }

    private void showMessage(String message, String bgColor) {
        successLabel.setText(message);
        successLabel.setStyle(String.format(
            "-fx-padding: 15; -fx-background-radius: 8; -fx-background-color: %s; " +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;", bgColor));
        successLabel.setVisible(true);
        
        // Auto-hide after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> successLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void clear() {
        firstName.clear(); 
        lastName.clear(); 
        gender.getSelectionModel().clearSelection();
        birthDate.setValue(null); 
        email.clear(); 
        phone.clear(); 
        address.clear();
        plan.getSelectionModel().clearSelection(); 
        quantity.getSelectionModel().select(Integer.valueOf(1));
        startAt.setValue(LocalDate.now());
    }

    public Node getRoot() { return scrollPane; }
}