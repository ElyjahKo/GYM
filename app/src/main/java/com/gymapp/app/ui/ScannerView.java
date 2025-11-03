package com.gymapp.app.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class ScannerView {
    private final BorderPane root = new BorderPane();

    public ScannerView() {
        root.setPadding(new Insets(16));
        root.setCenter(new Label("Le scan se fait désormais via l'URL de check-in (serveur local).\nOuvrez le Dashboard pour le QR du lien."));
    }

    public Node getRoot() { return root; }
}
