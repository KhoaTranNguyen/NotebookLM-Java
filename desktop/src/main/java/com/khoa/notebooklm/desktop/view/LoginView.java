package com.khoa.notebooklm.desktop.view;

import com.khoa.notebooklm.desktop.controller.AuthController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import com.khoa.notebooklm.desktop.model.dao.UserDao;

public class LoginView {

    private final VBox root = new VBox();

    public LoginView() {
        root.getStyleClass().addAll("cds-container", "cds-section");
        root.setAlignment(Pos.CENTER);

        Label title = new Label("NotebookLM Desktop");
        title.getStyleClass().add("cds-title");

        Label subtitle = new Label("Login to continue");
        subtitle.getStyleClass().add("cds-subtitle");

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setId("login-username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setId("login-password");

        // Registration fields (initially hidden)
        PasswordField confirmPass = new PasswordField(); confirmPass.setPromptText("Confirm password");
        TextField firstName = new TextField(); firstName.setPromptText("First name");
        TextField lastName = new TextField(); lastName.setPromptText("Last name");
        TextField email = new TextField(); email.setPromptText("Email");
        DatePicker dob = new DatePicker(); dob.setPromptText("Date of birth");
        dob.setMaxWidth(Double.MAX_VALUE); // Make date picker fill width

        VBox regBox = new VBox(16);
        regBox.getChildren().addAll(confirmPass, firstName, lastName, email, dob);
        regBox.setVisible(false);
        regBox.managedProperty().bind(regBox.visibleProperty());

        Label message = new Label();
        message.setTextFill(Color.web("#da1e28"));
        message.setVisible(false);

        Button primaryBtn = new Button("Login");
        primaryBtn.setDefaultButton(true);
        primaryBtn.setMaxWidth(Double.MAX_VALUE);
        
        Button switchMode = new Button("Create account");
        switchMode.getStyleClass().add("secondary");
        switchMode.setMaxWidth(Double.MAX_VALUE);
        
        VBox actions = new VBox(16, primaryBtn, switchMode);
        actions.setAlignment(Pos.CENTER);

        final boolean[] isRegister = { false };

        primaryBtn.setOnAction(e -> {
            message.setVisible(false);
            if (!isRegister[0]) {
                boolean ok = new AuthController().login(username.getText(), password.getText());
                if (ok) {
                    UserDao ud = new UserDao();
                    long userId = ud.getUserIdByUsername(username.getText());
                    if (userId <= 0) {
                        message.setText("Unable to resolve user id");
                        message.setVisible(true);
                        return;
                    }
                    root.getScene().setRoot(new MainView(userId).getRoot());
                } else {
                    message.setText("Invalid credentials");
                    message.setVisible(true);
                }
            } else {
                // Registration flow
                if (username.getText().isBlank() || password.getText().isBlank() || confirmPass.getText().isBlank()) {
                    message.setText("Fill required fields"); message.setVisible(true); return;
                }
                if (!password.getText().equals(confirmPass.getText())) {
                    message.setText("Passwords do not match"); message.setVisible(true); return;
                }
                try {
                    new UserDao().createUser(
                            username.getText(),
                            password.getText(),
                            firstName.getText(),
                            lastName.getText(),
                            email.getText(),
                            dob.getValue()
                    );
                    message.setTextFill(Color.web("#198038"));
                    message.setText("Account created. You can login now.");
                    message.setVisible(true);
                    isRegister[0] = false;
                    regBox.setVisible(false);
                    primaryBtn.setText("Login");
                    switchMode.setText("Create account");
                    message.setTextFill(Color.web("#198038"));
                } catch (Exception ex) {
                    message.setTextFill(Color.web("#da1e28"));
                    message.setText(ex.getMessage());
                    message.setVisible(true);
                }
            }
        });

        switchMode.setOnAction(e -> {
            isRegister[0] = !isRegister[0];
            regBox.setVisible(isRegister[0]);
            primaryBtn.setText(isRegister[0] ? "Register" : "Login");
            switchMode.setText(isRegister[0] ? "Back to login" : "Create account");
            subtitle.setText(isRegister[0] ? "Create an account" : "Login to continue");
            message.setVisible(false);
        });

        VBox card = new VBox();
        card.getStyleClass().addAll("cds-card", "cds-section");
        card.getChildren().addAll(title, subtitle, username, password, regBox, actions, message);
        card.setMaxWidth(400);
        root.getChildren().add(card);
    }

    public Parent getRoot() {
        return root;
    }
}
