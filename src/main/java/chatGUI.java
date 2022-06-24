import java.net.URL;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class chatGUI extends Application {
	@Override
	public void start(Stage primaryStage) {

		try {
			// Read file fxml and draw interface.
			// set the start page
			URL url = Paths.get("../resources/FXML/startFXML.fxml").toUri().toURL();
			Parent root = FXMLLoader.load(url);
			// if stage is closed
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					Platform.exit();
					primaryStage.close();
					System.exit(0);
				}
			});

			primaryStage.setTitle("chat system");
			Scene s = new Scene(root, 800,600);
			primaryStage.setScene(s);
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
