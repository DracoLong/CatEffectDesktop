/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cams;

import java.io.IOException;
import javafx.concurrent.Worker.State;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

// import com.google.zxing.BarcodeFormat;

/**
 * FXML Controller class
 *
 * @author dreamrunner
 */
public class MainFormController implements Initializable {

    private Stage selfStage;
    
    MainFormController mainFormController;
    
    @FXML
    private WebView webView;
    
    @FXML
    private WebView sideBar;
    
    private static WebEngine sideBarEngine, webEngine;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mainFormController = this;
        
        Font.loadFont(
            getClass().getResource("fonts/glyphicons-halflings-regular.ttf").toExternalForm(), 
            10
        );
        Font.loadFont(
            getClass().getResource("fonts/Telex-Regular.ttf").toExternalForm(), 
            10
        );
        
        EventHandler<WebEvent<String>> alertHandler = new EventHandler<WebEvent<String>> () {

            @Override
            public void handle(WebEvent<String> t) {
                System.out.println(t.getData());
            }
            
        };
        
        sideBar.setVisible(false);
        
        sideBarEngine = sideBar.getEngine();
        
        ChangeListener sideBarListener = new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                System.out.println(" SideBar Stage: " + newState.toString());
                JSObject jsobj = (JSObject)sideBarEngine.executeScript("window");
                jsobj.setMember("java", new Bridge());
                if (newState == Worker.State.SUCCEEDED) {
                    sideBar.setVisible(true);
                    sideBarEngine.executeScript("javaPageLoad();");
                }
            }
        };
        
        sideBarEngine.getLoadWorker().stateProperty().addListener(sideBarListener);
        
        webEngine = webView.getEngine();
        webView.setVisible(false);
        ChangeListener webViewListener = new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov,
                State oldState, State newState) {
                System.out.println(" WebView Stage: " + newState.toString());
                JSObject jsobj = (JSObject)webEngine.executeScript("window");
                jsobj.setMember("java", new Bridge());
                if (newState == Worker.State.SUCCEEDED) {
                    webView.setVisible(true);
                    webEngine.executeScript("javaPageLoad();");
                }
            }
        };
        
        webEngine.getLoadWorker().stateProperty().addListener(webViewListener);
        webEngine.setOnAlert(alertHandler);
        
        if (ServerInterface.isSystem()) {
            sideBarEngine.load(ServerInterface.getUrl("/system/sidebar"));
            webEngine.load(ServerInterface.getUrl("/system/dashboard"));
        } else {
            sideBarEngine.load(ServerInterface.getUrl("/stall/sidebar"));
            webEngine.load(ServerInterface.getUrl("/stall/dashboard"));
        }
        
        // webEngine.load("http://jqueryui.com/sortable/");
    }    
    
    public void setStage(Stage stage) {
        selfStage = stage;
    }
    
    public void refreshWebView() {
        webEngine.executeScript("location.reload()");
    }
    
    public class Bridge {
        public String getUsername() {
            return ServerInterface.getUsername();
        }
        public String getPassword() {
            return ServerInterface.getPassword();
        }
        public void navigate(String target) {
            webEngine.load(ServerInterface.getUrl(target));
        }
        public void open(String target, int width, int height) throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PopUpForm.fxml"));
            Parent root = (Parent)loader.load();
            PopUpFormController controller = (PopUpFormController)loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.setTarget(target);
            controller.setMainFormController(mainFormController);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setScene(scene);
            stage.setTitle("Loading... - CaMS@BTU");
            stage.setScene(scene);
            stage.show();
        }
        public void exit() throws IOException {
            selfStage.close();FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginForm.fxml"));
            Stage stage = new Stage();
            Parent root = (Parent)loader.load();
            LoginFormController controller = (LoginFormController)loader.getController();
            controller.setStage(stage);
            Scene scene = new Scene(root);
            scene.setFill(null);
            stage.setScene(scene);
            stage.setTitle("CaMS@BTU - Welcome");
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();
        }
        public void setFullScreen(boolean value) {
            selfStage.setFullScreen(value);
        }
        public void setMenu(int id) {
            sideBarEngine.executeScript("setMenu(" + id +  ");");
        }
        public String getAccountString() {
            String str;
            str = QRScanner.getText();
            System.out.println(str);
            return str;
        }
        
    }
}
