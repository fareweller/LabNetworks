package ru.com.miet.mp45.NetworkProject.Lab1.ClientGUI;

import com.sun.nio.sctp.MessageInfo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;

import javax.xml.soap.Text;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by stalker20210 on 12.10.2015.
 */
public class Controller {
    private ClientGui app;

    @FXML
    private TextField nickNameField;

    @FXML
    private TextField serverIPField;

    @FXML
    private TextField serverPortField;

    @FXML
    private TextField messageField;

    @FXML
    private ListView<String> clientsListView;

    @FXML
    private ListView<String> messagesListView;

    @FXML
    private Button connectToServerButton;

    @FXML
    private Button disconnectFromServerButton;

    @FXML
    private Button sendMessageButton;

    @FXML
    private void connectToServer() {
        String name = nickNameField.getText().trim();
        String serverIP = serverIPField.getText().trim();
        String serverPort = serverPortField.getText().trim();
        if (name.equals("") || serverIP.equals("") || serverPort.equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Не все поля заполнены!");
            alert.showAndWait();
            return;
        }
        try {
            int p = Integer.parseInt(serverPort);
            app.getClient().connectToServer(InetAddress.getByName(serverIP), p, name);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void disconnectFromServer() {
        try {
            app.getClient().disconnectFromServer();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText();
        app.getClient().sendString(message);
    }

    @FXML
    private void initialize() {

    }

    public void setMain(ClientGui app) {
        this.app = app;
        clientsListView.setItems(app.getConnectedClients());
        messagesListView.setItems(app.getReceivedMessages());
        sendMessageButton.disableProperty().bindBidirectional(app.getClient().isNotConnected());
        connectToServerButton.disableProperty().bindBidirectional(app.getClient().isConnected());
        disconnectFromServerButton.disableProperty().bindBidirectional(app.getClient().isNotConnected());
    }
}
