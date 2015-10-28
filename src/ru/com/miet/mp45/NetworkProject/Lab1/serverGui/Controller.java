package ru.com.miet.mp45.NetworkProject.Lab1.serverGui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.IOException;

public class Controller {
    private ServerGui app;
    @FXML
    private Button banButton;
    @FXML
    private Button unbanButton;
    @FXML
    private ListView<String> usersList;
    @FXML
    private ListView<String> banList;

    @FXML
    private void banUser(){
        try {
            app.getServer().banClient(usersList.getSelectionModel().getSelectedItem());
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
    @FXML
    private void unbanUser(){
            app.getServer().unbanClient(banList.getSelectionModel().getSelectedItem());
    }
    @FXML
    private void goodSelect(){
        unbanButton.setDisable(true);
        banButton.setDisable(false);
    }
    @FXML
    private void naughtySelect(){
        banButton.setDisable(true);
        unbanButton.setDisable(false);
    }
    @FXML
    private void unselectAll() {
        unbanButton.setDisable(true);
        banButton.setDisable(true);
    }
    public void setMain(ServerGui app) {
        this.app = app;
        usersList.setItems(app.getConnectedClients());
        banList.setItems(app.getBannedClients());
    }
}
