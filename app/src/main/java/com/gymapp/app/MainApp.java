package com.gymapp.app;

import com.gymapp.app.data.Member;
import com.gymapp.app.http.HttpServerStarter;
import com.gymapp.app.ui.DashboardView;
import com.gymapp.app.ui.MemberView;
import com.gymapp.app.ui.SubscriptionListView;
import com.gymapp.app.ui.SubscriptionNewView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
//
// cleaned duplicate imports

public class MainApp extends Application {
    private HttpServerStarter httpServer;

    @Override
    public void start(Stage primaryStage) {
        var root = new BorderPane();
        var tabs = new TabPane();
        // Start embedded HTTP server for QR check-in
        httpServer = new HttpServerStarter();
        httpServer.start(8080);

        var dashboard = new Tab("Dashboard", new DashboardView().getRoot());
        dashboard.setClosable(false);
        var clientele = new Tab("Clientèle", new MemberView().getRoot());
        clientele.setClosable(false);
        var newSub = new Tab("Nouvel Abonnement", new SubscriptionNewView().getRoot());
        newSub.setClosable(false);
        var subList = new Tab("Abonnements");
        subList.setClosable(false);
        subList.setContent(new Label("Chargement en cours..."));
        subList.setOnSelectionChanged(ev -> {
            if (subList.isSelected() && (subList.getContent() == null || subList.getContent() instanceof Label)) {
                try {
                    var view = new SubscriptionListView();
                    subList.setContent(view.getRoot());
                } catch (Exception ex) {
                    subList.setContent(new Label("Erreur de chargement de la liste d'abonnements"));
                }
            }
        });
        tabs.getTabs().addAll(dashboard, clientele, newSub, subList);
        // Top App Bar with logo and brand
        HBox appbar = new HBox(10);
        appbar.getStyleClass().add("appbar");
        appbar.setAlignment(Pos.CENTER_LEFT);
        ImageView logoView = null;
        try {
            var logoUrl = getClass().getResource("/logo.png");
            if (logoUrl != null) {
                logoView = new ImageView(logoUrl.toExternalForm());
                logoView.setFitHeight(24);
                logoView.setPreserveRatio(true);
            }
        } catch (Exception ignored) {}
        Label brand = new Label("DFD GYM");
        brand.getStyleClass().add("brand-title");
        if (logoView != null) appbar.getChildren().addAll(logoView, brand); else appbar.getChildren().add(brand);

        VBox content = new VBox(appbar, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        root.setCenter(content);
        var scene = new Scene(root, 1000, 700);
        try {
            var css = getClass().getResource("/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}
        primaryStage.setTitle("DFD GYM");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }
}
