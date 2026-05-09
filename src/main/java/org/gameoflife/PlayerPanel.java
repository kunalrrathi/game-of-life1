//package org.gameoflife;
//
//import javafx.geometry.Insets;
//import javafx.scene.control.Label;
//import javafx.scene.layout.VBox;
//
//public class PlayerPanel {
//
//    private VBox panel;
//
//    private Label cashLabel;
//    private Label notesLabel;
//
//    private Label fireInsurance;
//    private Label autoInsurance;
//    private Label stockInsurance;
//    private Label lifeInsurance;
//
//    private Label marriedLabel;
//    private Label childrenLabel;
//
//    private Label collectCards;
//    private Label payCards;
//    private Label exemptionCards;
//
//    public PlayerPanel() {
//
//        panel = new VBox(10);
//        panel.setPadding(new Insets(15));
//        panel.setPrefWidth(220);
//
//        panel.setStyle("-fx-background-color: #f4f4f4;");
//
//        Label title = new Label("Player Information");
//        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//
//        cashLabel = new Label("Cash: $10000");
//        notesLabel = new Label("Promissory Notes: 0");
//
//        Label insuranceTitle = new Label("Insurance");
//        fireInsurance = new Label("Fire: 0");
//        autoInsurance = new Label("Auto: 0");
//        stockInsurance = new Label("Stock: 0");
//        lifeInsurance = new Label("Life: 0");
//
//        Label familyTitle = new Label("Family");
//        marriedLabel = new Label("Married: No");
//        childrenLabel = new Label("Children: 0");
//
//        Label shareTitle = new Label("Share The Wealth");
//        collectCards = new Label("Collect Cards: 0");
//        payCards = new Label("Pay Cards: 0");
//        exemptionCards = new Label("Exemption Cards: 0");
//
//        panel.getChildren().addAll(
//                title,
//                cashLabel,
//                notesLabel,
//
//                insuranceTitle,
//                fireInsurance,
//                autoInsurance,
//                stockInsurance,
//                lifeInsurance,
//
//                familyTitle,
//                marriedLabel,
//                childrenLabel,
//
//                shareTitle,
//                collectCards,
//                payCards,
//                exemptionCards
//        );
//    }
//
//    public VBox getPanel() {
//        return panel;
//    }
//}