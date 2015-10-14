package ru.com.miet.mp45.NetworkProject.Lab1.ClientGUI;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.com.miet.mp45.NetworkProject.Lab1.Client;

import java.io.IOException;

public class ClientGui extends Application {

    private Stage primaryStage = null;
    private VBox rootLayout = null;

    private Client client = null;
    private ObservableList<String> receivedMessaged = FXCollections.observableArrayList();
    private ObservableList<String> connectedClients = FXCollections.observableArrayList();

    public static void Main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Hail Satan!");
        this.primaryStage.setResizable(false);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(ClientGui.class.getResource("ClientGUI.fxml"));
            rootLayout = fxmlLoader.load();

            Scene scene = new Scene(rootLayout);

            primaryStage.setScene(scene);
            primaryStage.show();

            client = new Client(55555, this);
            (new Thread(client)).start();

            this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    try {
                        client.turnOff();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            Controller controller = fxmlLoader.getController();
            controller.setMain(this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Client getClient() {
        return client;
    }

    public ObservableList<String> getReceivedMessaged() {
        return receivedMessaged;
    }

    public ObservableList<String> getConnectedClients() {
        return connectedClients;
    }
}
