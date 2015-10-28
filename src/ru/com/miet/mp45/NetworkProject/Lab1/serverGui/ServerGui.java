package ru.com.miet.mp45.NetworkProject.Lab1.serverGui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.com.miet.mp45.NetworkProject.Lab1.Server;

import java.io.IOException;

public class ServerGui extends Application {

    private Server server = null;
    private AnchorPane rootLayout = null;
    private Stage primaryStage = null;

    private ObservableList<String> bannedClients = FXCollections.observableArrayList();
    private ObservableList<String> connectedClients = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            this.primaryStage = primaryStage;
            this.primaryStage.setTitle("Hail Satan!");
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(ServerGui.class.getResource("serverGui.fxml"));
            rootLayout = fxmlLoader.load();

            Scene scene = new Scene(rootLayout);

            primaryStage.setScene(scene);
            primaryStage.show();

            server = new Server(44444, this);
            (new Thread(server)).start();

            this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    try {
                        server.turnOff();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            Controller controller = fxmlLoader.getController();
            controller.setMain(this);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
    public Server getServer() {
        return server;
    }
    public ObservableList<String> getConnectedClients() {
        return connectedClients;
    }
    public ObservableList<String> getBannedClients(){return  bannedClients;}
}
