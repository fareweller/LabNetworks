package ru.com.miet.mp45.NetworkProject.Lab1.serverGui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class Controller {
    @FXML
    private Button banButton;
    @FXML
    private Button unbanButton;
    @FXML
    private ListView goodList;
    @FXML
    private ListView naughtyList;

    @FXML
    private void banPerson(){

    }
    @FXML
    private void unbanPerson(){

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

}
