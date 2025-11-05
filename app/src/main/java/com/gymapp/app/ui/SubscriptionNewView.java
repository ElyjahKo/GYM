package com.gymapp.app.ui;

import com.gymapp.app.data.Member;
import com.gymapp.app.data.MemberService;
import com.gymapp.app.data.SubscriptionService;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.time.LocalDate;

public class SubscriptionNewView {
    private final MemberService memberService = new MemberService();
    private final SubscriptionService subService = new SubscriptionService();

    private final ScrollPane scrollPane = new ScrollPane();
    private final StackPane root = new StackPane();
    private final VBox mainContent = new VBox(30);
    private final StackPane toastContainer = new StackPane();

    // Wizard state
    private int currentStep = 0;
    private final int totalSteps = 3;
    
    // Step containers
    private final VBox step1Container = new VBox(20);
    private final VBox step2Container = new VBox(20);
    private final VBox step3Container = new VBox(20);
    
    // Member fields
    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final ComboBox<String> gender = new ComboBox<>();
    private final DatePicker birthDate = new DatePicker();
    private final TextField email = new TextField();
    private final TextField phone = new TextField();
    private final TextField address = new TextField();
    private final TextField weight = new TextField();
    private final TextField height = new TextField();

    // Subscription fields
    private final ToggleGroup planGroup = new ToggleGroup();
    private final ComboBox<Integer> quantity = new ComboBox<>();
    private final DatePicker startAt = new DatePicker(LocalDate.now());
    private final TextField priceField = new TextField();
    
    // Navigation buttons
    private final Button prevBtn = new Button("← Précédent");
    private final Button nextBtn = new Button("Suivant →");
    private final Button submitBtn = new Button("✓ Créer l'Abonnement");
    
    // Progress indicator
    private final HBox progressIndicator = new HBox(15);

    public SubscriptionNewView() {
        root.setStyle("-fx-background-color: transparent;");
        build();
        showStep(0);
        
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private void build() {
        mainContent.setPadding(new Insets(30));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setMaxWidth(900);
        
        // === HEADER ===
        VBox header = createHeader();
        
        // === PROGRESS INDICATOR ===
        buildProgressIndicator();
        
        // === BUILD STEPS ===
        buildStep1();
        buildStep2();
        buildStep3();
        
        // === NAVIGATION ===
        HBox navigation = createNavigation();
        
        // === ASSEMBLY ===
        mainContent.getChildren().addAll(
            header,
            progressIndicator,
            step1Container,
            step2Container,
            step3Container,
            navigation
        );
        
        // Toast container
        toastContainer.setAlignment(Pos.TOP_RIGHT);
        toastContainer.setPadding(new Insets(20));
        toastContainer.setPickOnBounds(false);
        
        root.getChildren().addAll(mainContent, toastContainer);
        StackPane.setAlignment(toastContainer, Pos.TOP_RIGHT);
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        
        // Animated icon
        FontIcon icon = new FontIcon(FontAwesomeSolid.USER_PLUS);
        icon.setIconSize(48);
        icon.setIconColor(Color.web("#667eea"));
        
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.5), icon);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();
        
        Label title = new Label("Nouvel Abonnement");
        title.getStyleClass().add("header-title");
        
        Label subtitle = new Label("Créez un nouveau membre et son abonnement en 3 étapes simples");
        subtitle.setFont(Font.font("Inter", FontWeight.NORMAL, 15));
        subtitle.setTextFill(Color.web("#a0aec0"));
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(600);
        subtitle.setAlignment(Pos.CENTER);
        
        header.getChildren().addAll(icon, title, subtitle);
        return header;
    }

    private void buildProgressIndicator() {
        progressIndicator.setAlignment(Pos.CENTER);
        progressIndicator.setPadding(new Insets(20, 0, 20, 0));
        updateProgressIndicator();
    }

    private void updateProgressIndicator() {
        progressIndicator.getChildren().clear();
        
        String[] stepLabels = {"Informations", "Abonnement", "Confirmation"};
        FontAwesomeSolid[] stepIcons = {
            FontAwesomeSolid.USER,
            FontAwesomeSolid.CREDIT_CARD,
            FontAwesomeSolid.CHECK_CIRCLE
        };
        
        for (int i = 0; i < totalSteps; i++) {
            final int stepIndex = i;
            boolean isActive = i <= currentStep;
            boolean isCurrent = i == currentStep;
            
            // Circle with number
            Circle circle = new Circle(25);
            circle.setFill(isActive ? Color.web("#667eea") : Color.web("#1a1a2e"));
            circle.setStroke(Color.web("#667eea"));
            circle.setStrokeWidth(2);
            
            FontIcon stepIcon = new FontIcon(stepIcons[i]);
            stepIcon.setIconSize(18);
            stepIcon.setIconColor(isActive ? Color.WHITE : Color.web("#718096"));
            
            StackPane circleStack = new StackPane(circle, stepIcon);
            
            // Pulse animation for current step
            if (isCurrent) {
                ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), circleStack);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.15);
                pulse.setToY(1.15);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();
            }
            
            Label label = new Label(stepLabels[i]);
            label.setTextFill(isActive ? Color.web("#667eea") : Color.web("#718096"));
            label.setFont(Font.font("Inter", FontWeight.BOLD, 13));
            
            VBox stepBox = new VBox(10, circleStack, label);
            stepBox.setAlignment(Pos.CENTER);
            
            progressIndicator.getChildren().add(stepBox);
            
            // Add connecting line
            if (i < totalSteps - 1) {
                Line line = new Line(0, 0, 100, 0);
                line.setStroke(i < currentStep ? Color.web("#667eea") : Color.web("#1a1a2e"));
                line.setStrokeWidth(3);
                
                StackPane lineContainer = new StackPane(line);
                lineContainer.setAlignment(Pos.CENTER);
                lineContainer.setPrefWidth(100);
                
                progressIndicator.getChildren().add(lineContainer);
            }
        }
    }

    private void buildStep1() {
        step1Container.getStyleClass().add("card");
        step1Container.setAlignment(Pos.TOP_CENTER);
        step1Container.setMaxWidth(700);
        
        Label stepTitle = new Label("👤 Informations du Client");
        stepTitle.setFont(Font.font("Inter", FontWeight.BOLD, 22));
        stepTitle.setTextFill(Color.WHITE);
        
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(102, 126, 234, 0.2);");
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);
        
        gender.getItems().addAll("Homme", "Femme", "Autre");
        gender.setPromptText("Sélectionnez le genre");
        
        int r = 0;
        grid.add(createFormField("Prénom", firstName, FontAwesomeSolid.USER), 0, r);
        grid.add(createFormField("Nom", lastName, FontAwesomeSolid.USER), 1, r++);
        grid.add(createFormField("Genre", gender, FontAwesomeSolid.VENUS_MARS), 0, r);
        grid.add(createFormField("Date de Naissance", birthDate, FontAwesomeSolid.BIRTHDAY_CAKE), 1, r++);
        grid.add(createFormField("Email", email, FontAwesomeSolid.ENVELOPE), 0, r, 2, 1); r++;
        grid.add(createFormField("Téléphone", phone, FontAwesomeSolid.PHONE), 0, r);
        grid.add(createFormField("Adresse", address, FontAwesomeSolid.MAP_MARKER_ALT), 1, r++);
        grid.add(createFormField("Poids (kg)", weight, FontAwesomeSolid.WEIGHT), 0, r);
        grid.add(createFormField("Taille (cm)", height, FontAwesomeSolid.RULER_VERTICAL), 1, r++);
        
        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);
        
        step1Container.getChildren().addAll(stepTitle, sep, grid);
    }

    private void buildStep2() {
        step2Container.getStyleClass().add("card");
        step2Container.setAlignment(Pos.TOP_CENTER);
        step2Container.setMaxWidth(700);
        
        Label stepTitle = new Label("💳 Détails de l'Abonnement");
        stepTitle.setFont(Font.font("Inter", FontWeight.BOLD, 22));
        stepTitle.setTextFill(Color.WHITE);
        
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(102, 126, 234, 0.2);");
        
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        
        // Plan selection with cards
        Label planLabel = new Label("Sélectionnez un forfait");
        planLabel.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 16));
        planLabel.setTextFill(Color.web("#a0aec0"));
        
        HBox planCards = new HBox(20);
        planCards.setAlignment(Pos.CENTER);
        
        VBox monthlyCard = createPlanCard("📅 Mensuel", "1 mois", "15 000 FCFA", "#667eea");
        VBox quarterlyCard = createPlanCard("📊 Trimestriel", "3 mois", "40 000 FCFA", "#00f2c3");
        VBox annualCard = createPlanCard("⭐ Annuel", "12 mois", "150 000 FCFA", "#feca57");
        
        planCards.getChildren().addAll(monthlyCard, quarterlyCard, annualCard);
        
        // Duration and details
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(20);
        detailsGrid.setAlignment(Pos.CENTER);
        
        quantity.getItems().addAll(1, 3, 6, 12);
        quantity.getSelectionModel().select(Integer.valueOf(1));
        quantity.setPromptText("Durée en mois");
        
        priceField.setPromptText("Montant en FCFA");
        
        int r = 0;
        detailsGrid.add(createFormField("Durée (mois)", quantity, FontAwesomeSolid.CALENDAR_ALT), 0, r);
        detailsGrid.add(createFormField("Date de début", startAt, FontAwesomeSolid.CALENDAR_CHECK), 1, r++);
        detailsGrid.add(createFormField("Montant (FCFA)", priceField, FontAwesomeSolid.MONEY_BILL_WAVE), 0, r, 2, 1);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        detailsGrid.getColumnConstraints().addAll(col1, col2);
        
        content.getChildren().addAll(planLabel, planCards, detailsGrid);
        step2Container.getChildren().addAll(stepTitle, sep, content);
    }

    private VBox createPlanCard(String title, String duration, String price, String accentColor) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(200, 180);
        card.setStyle(card.getStyle() + "-fx-border-color: " + accentColor + "; -fx-border-width: 2;");
        
        RadioButton radio = new RadioButton();
        radio.setToggleGroup(planGroup);
        radio.setUserData(duration);
        if (title.contains("Mensuel")) radio.setSelected(true);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        
        Label durationLabel = new Label(duration);
        durationLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        durationLabel.setTextFill(Color.web("#a0aec0"));
        
        Label priceLabel = new Label(price);
        priceLabel.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        priceLabel.setTextFill(Color.web(accentColor));
        
        card.getChildren().addAll(radio, titleLabel, durationLabel, priceLabel);
        
        // Hover effect
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
        
        card.setOnMouseClicked(e -> radio.setSelected(true));
        
        return card;
    }

    private void buildStep3() {
        step3Container.getStyleClass().add("card");
        step3Container.setAlignment(Pos.TOP_CENTER);
        step3Container.setMaxWidth(700);
        
        Label stepTitle = new Label("✓ Confirmation");
        stepTitle.setFont(Font.font("Inter", FontWeight.BOLD, 22));
        stepTitle.setTextFill(Color.WHITE);
        
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(102, 126, 234, 0.2);");
        
        VBox summaryBox = new VBox(20);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        
        Label summaryTitle = new Label("📋 Récapitulatif");
        summaryTitle.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        summaryTitle.setTextFill(Color.web("#667eea"));
        
        // Summary will be populated dynamically
        VBox memberSummary = new VBox(10);
        memberSummary.setId("memberSummary");
        
        VBox subSummary = new VBox(10);
        subSummary.setId("subSummary");
        
        summaryBox.getChildren().addAll(summaryTitle, memberSummary, subSummary);
        
        step3Container.getChildren().addAll(stepTitle, sep, summaryBox);
    }

    private VBox createFormField(String labelText, Control input, FontAwesomeSolid iconType) {
        VBox field = new VBox(8);
        
        HBox labelBox = new HBox(8);
        labelBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#667eea"));
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 13));
        label.setTextFill(Color.web("#a0aec0"));
        
        labelBox.getChildren().addAll(icon, label);
        
        field.getChildren().addAll(labelBox, input);
        HBox.setHgrow(field, Priority.ALWAYS);
        
        if (input instanceof TextField) {
            ((TextField) input).setPromptText("Saisissez " + labelText.toLowerCase());
        }
        
        return field;
    }

    private HBox createNavigation() {
        HBox nav = new HBox(15);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(20, 0, 0, 0));
        
        prevBtn.getStyleClass().addAll("button", "btn-secondary");
        prevBtn.setPrefWidth(160);
        prevBtn.setOnAction(e -> previousStep());
        
        nextBtn.getStyleClass().addAll("button", "btn-primary");
        nextBtn.setPrefWidth(160);
        nextBtn.setOnAction(e -> nextStep());
        
        submitBtn.getStyleClass().addAll("button", "btn-success");
        submitBtn.setPrefWidth(220);
        submitBtn.setOnAction(e -> submitForm());
        submitBtn.setVisible(false);
        
        nav.getChildren().addAll(prevBtn, nextBtn, submitBtn);
        return nav;
    }

    private void showStep(int step) {
        currentStep = step;
        
        step1Container.setVisible(step == 0);
        step1Container.setManaged(step == 0);
        
        step2Container.setVisible(step == 1);
        step2Container.setManaged(step == 1);
        
        step3Container.setVisible(step == 2);
        step3Container.setManaged(step == 2);
        
        prevBtn.setVisible(step > 0);
        nextBtn.setVisible(step < totalSteps - 1);
        submitBtn.setVisible(step == totalSteps - 1);
        
        if (step == 2) {
            updateSummary();
        }
        
        updateProgressIndicator();
        
        // Scroll to top with animation
        scrollPane.setVvalue(0);
    }

    private void nextStep() {
        if (currentStep == 0 && !validateStep1()) {
            return;
        }
        if (currentStep == 1 && !validateStep2()) {
            return;
        }
        
        if (currentStep < totalSteps - 1) {
            // Fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), mainContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                showStep(currentStep + 1);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mainContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }

    private void previousStep() {
        if (currentStep > 0) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), mainContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                showStep(currentStep - 1);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mainContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }

    private boolean validateStep1() {
        if (firstName.getText().isBlank() || lastName.getText().isBlank()) {
            showToast("⚠️ Veuillez saisir le prénom et le nom", "warning");
            return false;
        }
        if (gender.getValue() == null) {
            showToast("⚠️ Veuillez sélectionner le genre", "warning");
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (quantity.getValue() == null || startAt.getValue() == null) {
            showToast("⚠️ Veuillez renseigner tous les champs", "warning");
            return false;
        }
        return true;
    }

    private void updateSummary() {
        VBox memberSummary = (VBox) step3Container.lookup("#memberSummary");
        VBox subSummary = (VBox) step3Container.lookup("#subSummary");
        
        if (memberSummary != null) {
            memberSummary.getChildren().clear();
            memberSummary.getChildren().addAll(
                createSummaryCard("👤 Client", String.format("%s %s", firstName.getText(), lastName.getText())),
                createSummaryCard("⚧ Genre", gender.getValue() != null ? gender.getValue() : "—"),
                createSummaryCard("📧 Email", email.getText().isBlank() ? "—" : email.getText()),
                createSummaryCard("📱 Téléphone", phone.getText().isBlank() ? "—" : phone.getText())
            );
        }
        
        if (subSummary != null) {
            subSummary.getChildren().clear();
            RadioButton selected = (RadioButton) planGroup.getSelectedToggle();
            String plan = selected != null ? selected.getUserData().toString() : "1 mois";
            
            subSummary.getChildren().addAll(
                createSummaryCard("📋 Forfait", plan),
                createSummaryCard("🔢 Durée", quantity.getValue() + " mois"),
                createSummaryCard("📅 Début", startAt.getValue().toString()),
                createSummaryCard("💰 Montant", priceField.getText().isBlank() ? "0 FCFA" : priceField.getText() + " FCFA")
            );
        }
    }

    private HBox createSummaryCard(String label, String value) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(102, 126, 234, 0.1); -fx-padding: 15; -fx-background-radius: 10;");
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        labelText.setTextFill(Color.web("#a0aec0"));
        labelText.setPrefWidth(150);
        
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        valueText.setTextFill(Color.WHITE);
        HBox.setHgrow(valueText, Priority.ALWAYS);
        
        card.getChildren().addAll(labelText, valueText);
        return card;
    }

    private void submitForm() {
        try {
            Member m = new Member();
            m.setFirstName(firstName.getText().trim());
            m.setLastName(lastName.getText().trim());
            m.setFullName((firstName.getText().trim() + " " + lastName.getText().trim()).trim());
            m.setGender("Homme".equals(gender.getValue()) ? "H" : ("Femme".equals(gender.getValue()) ? "F" : "A"));
            m.setBirthDate(birthDate.getValue());
            m.setEmail(email.getText().trim());
            m.setPhone(phone.getText().trim());
            m.setAddress(address.getText().trim());
            
            try {
                if (!weight.getText().isBlank()) m.setWeightKg(Double.parseDouble(weight.getText()));
                if (!height.getText().isBlank()) m.setHeightCm(Double.parseDouble(height.getText()));
            } catch (NumberFormatException ignored) {}
            
            m.setSubscriptionStatus(Member.SubscriptionStatus.INACTIVE);

            m = memberService.save(m);
            
            RadioButton selected = (RadioButton) planGroup.getSelectedToggle();
            String planName = selected != null ? selected.getUserData().toString() : "Mensuel";
            
            int price = 0;
            try {
                if (!priceField.getText().isBlank()) {
                    price = Integer.parseInt(priceField.getText().replaceAll("[^0-9]", ""));
                }
            } catch (NumberFormatException ignored) {}
            
            subService.createInitial(m, planName, quantity.getValue(), price, startAt.getValue());

            showToast("✅ Client enregistré et abonnement créé avec succès!", "success");
            
            // Reset form after 2 seconds
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> clear()));
            timeline.play();
            
        } catch (Exception ex) {
            showToast("❌ Erreur: " + ex.getMessage(), "error");
        }
    }

    private void clear() {
        firstName.clear(); 
        lastName.clear(); 
        gender.getSelectionModel().clearSelection();
        birthDate.setValue(null); 
        email.clear(); 
        phone.clear(); 
        address.clear();
        weight.clear();
        height.clear();
        quantity.getSelectionModel().select(Integer.valueOf(1));
        startAt.setValue(LocalDate.now());
        priceField.clear();
        planGroup.selectToggle(null);
        
        showStep(0);
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

    public Node getRoot() { return scrollPane; }
}