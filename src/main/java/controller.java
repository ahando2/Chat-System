import java.nio.file.Paths;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.xml.soap.Text;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class controller implements Initializable {
    @FXML
    private VBox serverBox,clientListBox,chatBox,clientBox,errorBox;
    @FXML
    private HBox startBox,sendBox, listBox,idBox;
    @FXML
    private Button sendBtn;
    @FXML
    private TextField message;
    @FXML
    private Label idText;
    @FXML
    private ListView<Button> clientList;
    @FXML
    private ListView<String> chatList;
    @FXML
    private ListView<Object> serverList = new ListView<>();
    @FXML
    private ArrayList<Integer> recipientList = new ArrayList<>();
    @FXML
    private Client clientConnection;
    @FXML
    private Server serverConnection;

    private  Data data = new Data();

    // string button client style
    String buttonStyle = "-fx-font-size: 15px;" +
            "-fx-font-family: \"Helvetica\";" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 0;" +
            "-fx-background-color:rgb(238, 236, 234);" +
            "-fx-text-fill: #464646;" +
            "-fx-pref-width: 180;" +
            "-fx-pref-height: 50;";

    // string button client clicked style
    String buttonStyleClick = "-fx-font-size: 15px;" +
            "-fx-font-family: \"Helvetica\";" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 0;" +
            "-fx-background-color:rgb(234,161,151);" +
            "-fx-text-fill: #464646;" +
            "-fx-pref-width: 180;" +
            "-fx-pref-height: 50;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
    }

    //static so each instance of controller can access to update

    // action button for server button, go to server scene
    public void serverAction(ActionEvent e) throws IOException {
        //get instance of the loader class
        URL url = Paths.get("../resources/FXML/serverFXML.fxml").toUri().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        serverBox = loader.load(); //load view into parent
        controller ctr = loader.getController();// get the controller of the parent

        // set up server connection
        ctr.serverConnection = new Server(inData -> {
            // adding input to list view
            Platform.runLater(()->{
                ctr.serverList.getItems().add(inData.toString());
            });
        });
        startBox.getScene().setRoot(serverBox);//update scene graph
    }

    // action button for client button, go to client scene
    public void clientAction(ActionEvent t) throws IOException {
        URL url = Paths.get("../resources/FXML/clientFXML.fxml").toUri().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        clientBox = loader.load(); //load view into parent
        controller ctr = loader.getController();// get the controller of the parent

        // add everyone button to client list, set it as the first default
        ctr.recipientList.add(0);
        Button everyoneBtn = new Button("Everyone");
        everyoneBtn.setStyle(buttonStyleClick);
        everyoneBtn.setOnAction(clientListBtn(everyoneBtn,ctr));
        ctr.clientList.getItems().add(everyoneBtn);

        // set up client connection
        ctr.clientConnection = new Client(input->{
            Platform.runLater(()->{
                Data inData = (Data)input;

                // if server is down then go to error page
                if ( inData.message.equals("Server is down") && inData.sysMessage == 503 ) {
                    try {
                        errorAction(t,ctr);
                    } catch(IOException e) {}
                }
                // if sysMessage is 2 means it 's sending the available client(s) to a new client
                else if ( inData.sysMessage == 2 ){
                    int num = Integer.parseInt(inData.message);
                    Button button = new Button("Client #"+num);
                    button.setStyle(buttonStyle);
                    button.setOnAction(clientListBtn(button,ctr));
                    ctr.clientList.getItems().add(button);
                }
                // if sysMessage is 1 means it 's sending the new clients ( including this client)
                else if ( inData.sysMessage == 1){
                    ctr.chatList.getItems().add(inData.message);
                    int num = Integer.parseInt(inData.message.replaceFirst("new client client #",""));
                    // if this is the first time, it's the current client id number
                    if ( ctr.clientList.getItems().size() == 1 && ctr.idText.getText().equals("")){
                        ctr.idText.setText("You are Client #"+num);
                    }else {// else  it's the a new client id number, add them to client list
                        Button button = new Button("Client #"+num);
                        button.setStyle(buttonStyle);
                        button.setOnAction(clientListBtn(button,ctr));
                        ctr.clientList.getItems().add(button);
                    }
                }
                // if sysMessage is 3 then a client hop off the server, delete it from client list and recipient list if exist
                else if ( inData.sysMessage == 3 && inData.message.contains(" has left the server!")){
                    ctr.chatList.getItems().add(inData.message);
                    String num = inData.message.replaceFirst(" has left the server!","");
                    ctr.clientList.getItems().removeIf(button -> button.getText().equals( num)); // remove that button

                    // get removed client id number
                    Integer clientId = Integer.parseInt(num.replaceFirst("Client #",""));
                    if ( ctr.recipientList.contains(clientId)) {  // remove if its in the recipient list
                        ctr.recipientList.remove( clientId);
                    }

                    // if current client is the only client or the only click button is the removed client
                    // set everyone button to be clicked
                    if ( ctr.clientList.getItems().size() == 1 || ctr.recipientList.size() == 0 ){
                        ctr.recipientList.clear(); // clear recipient list
                        ctr.recipientList.add(0); // add everyone
                        ctr.clientList.getItems().get(0).setStyle(buttonStyleClick); // set the everyone button to be 'clicked'
                    }

                }else { // it's a message
                    ctr.chatList.getItems().add(inData.message); // add to the chat box
                }
            });
        });

        ctr.clientConnection.start(); // start the clientConnection
        ctr.sendBtn.setOnAction(sendAction(ctr)); // set the sendBtn action
        ctr.message.setOnKeyPressed(new EventHandler<KeyEvent>(){ // send message on Enter
            @Override
            public void handle(KeyEvent key){
                if (key.getCode().equals(KeyCode.ENTER)){
                    ctr.data.message = ctr.message.getText();
                    ctr.data.sysMessage = 0;
                    ctr.data.recipientId = ctr.recipientList;
                    ctr.clientConnection.send(ctr.data);
                    ctr.message.clear();
                }
            }
        });
        startBox.getScene().setRoot(clientBox);//update scene graph
    }

    EventHandler<ActionEvent> clientListBtn(Button button,controller ctr) {
        return e-> {
            button.setStyle(buttonStyleClick);
            if ( button.getText().equals("Everyone")){  // if it's an everyone button
                // clear the recipient list and set it to '0'
                ctr.recipientList.clear();
                ctr.recipientList.add(0);
                for (Button b:ctr.clientList.getItems()) {
                    if (!b.getText().equals("Everyone")) b.setStyle(buttonStyle); // set all button to be "unclick" except the everyone
                }
                // update scene
                Parent clientBoxNew = ctr.clientBox; //load view
                clientBox.getScene().setRoot(clientBoxNew);//update scene graph
            }else { // else  it's a client button
                // set everyone button to be 'unclicked'
                ctr.clientList.getItems().get(0).setStyle(buttonStyle);

                // remove everyone from recipient
                if (ctr.recipientList.contains(0)) ctr.recipientList.remove((Integer) 0);
                Integer num = Integer.parseInt(button.getText().replaceAll("Client #", ""));

                // add to recipient
                if (!ctr.recipientList.contains(num)) ctr.recipientList.add(num);

                // if re Clicked, remove from recipient
                button.setOnAction(j -> {

                    // if nothing left is clicked then set everyone as recipient
                    if (ctr.recipientList.size() <= 1 && ctr.recipientList.get(0)!=0) {
                        ctr.recipientList.clear();
                        ctr.recipientList.add(0);
                        ctr.clientList.getItems().get(0).setStyle(buttonStyleClick);
                    }
                    else ctr.recipientList.remove(num); // else remove normally
                    button.setOnAction(clientListBtn(button,ctr));
                    button.setStyle(buttonStyle); // set button to be 'unClick'

                    // update scene
                    Parent clientBoxN = ctr.clientBox; //load view
                    clientBox.getScene().setRoot(clientBoxN);//update scene graph
                });

            }
        };
    }

    // handle send button
    EventHandler<ActionEvent> sendAction(controller ctr)  {
        return e-> {
            //if message is 'empty' do nothing
            if (ctr.message.getText().equals("") || (ctr.message.getText().replace(" ", "").length()==0)){
                ctr.message.clear();
                return;
            }
            // else send the message to recipient(s)
            ctr.data.message = ctr.message.getText();
            ctr.data.sysMessage = 0;
            ctr.data.recipientId = ctr.recipientList;
            ctr.clientConnection.send(ctr.data);
            ctr.message.clear();
        };
    }

    // if server is down or not open, set to error scene then quit
    public void errorAction(ActionEvent e,controller ctr) throws IOException {
        URL url = Paths.get("../resources/FXML/errorFXML.fxml").toUri().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        errorBox = loader.load(); //load view into parent
        try {
            ctr.clientBox.getScene().setRoot(errorBox);//update scene graph
        } catch (Exception ignored) {}

        // delay for 3 seconds, then quit
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(3000), t-> {
            Platform.exit();
            System.exit(0);
        }));
        delay.play();
    }

}


