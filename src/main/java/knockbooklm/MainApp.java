package knockbooklm;

import knockbooklm.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("KnockbookLM");
        Scene scene = new Scene(new LoginView().getRoot(), 480, 640);
        scene.getStylesheets().add(getClass().getResource("/styles/carbon.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
