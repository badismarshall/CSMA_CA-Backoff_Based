/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import java.beans.PropertyChangeEvent;
import javafx.scene.control.Label;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * FXML Controller class
 *
 * @author me_ba
 */
public class SimulationMakerController implements Initializable{



    @FXML
    private JFXButton StartSimulation;
    @FXML
    private JFXButton StopSimulation;
    @FXML
    private AnchorPane drawPanel;

    // le id des station
    int id = 0;
    // liste des stations ajouter
    List<Station> Stations = new ArrayList<>();
    // liste des station qui veullent transmmtre
    List<Station> needSend = new ArrayList<>();
    // liste des station qui n'ont pas transmer le msj 
     List<Station> StationsWait = new ArrayList<>();
    
     static SequentialTransition timer;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        StopSimulation.setDisable(true);
    }    

    public List<Station> getStations() {
        return Stations;
    }

    @FXML
    private void addStationLisner(MouseEvent event) {
        System.out.println("is clicked");
        id += 1;
        Station station = new Station(id);
        Stations.add(station);  // ajouter a la liste des station
        // create circle
        Circle circle = new Circle();
        
                circle.setCenterX(event.getX());
                circle.setCenterY(event.getY());
                circle.setRadius(100);  // le rayon de cercle
                circle.setFill(Color.TRANSPARENT);
                circle.setStrokeWidth(2);
                circle.setStrokeMiterLimit(10);
                circle.setStrokeType(StrokeType.CENTERED);
                circle.setStyle("-fx-background-color:yellow");
                circle.setStroke(Color.BLACK);
                station.setCircle(circle);
        // add circle
        drawPanel.getChildren().add(circle);
        
        // create monitor image
        Image image = new Image("/application/monitor.png");
        ImageView imageView = new ImageView(image);
        imageView.setLayoutX(circle.getCenterX() - 20);
        imageView.setLayoutY(circle.getCenterY() - 30); 
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        station.setImage(imageView);
        drawPanel.getChildren().add(imageView);
        
        // Id Label
        Label label = new Label(Integer.toString(id));
        label.setLayoutX(circle.getCenterX()- 5);
        label.setLayoutY(circle.getCenterY() - 25); 
        label.setStyle("-fx-font-size:17;-fx-text-fill: black;-fx-font-weight: bold");
        drawPanel.getChildren().add(label);
        
        // ReadyToSend
        Label stateLabel = new Label("");
        stateLabel.setLayoutX(circle.getCenterX() - 30 );
        stateLabel.setLayoutY(circle.getCenterY() + 10); 
        stateLabel.setStyle("-fx-font-size:12;-fx-text-fill: black;-fx-font-weight: bold;");
        station.setReadytosend(stateLabel);
        stateLabel.setVisible(true);
        drawPanel.getChildren().add(stateLabel);
        
        //NeedToSend
        Label stateLabel2 = new Label("");
        stateLabel2.setLayoutX(circle.getCenterX() - 30 );
        stateLabel2.setLayoutY(circle.getCenterY() + 25); 
        stateLabel2.setStyle("-fx-font-size:12;-fx-text-fill: black;-fx-font-weight: bold;");
        stateLabel2.setVisible(true);
        station.setNeedToSend(stateLabel2);
        drawPanel.getChildren().add(stateLabel2);
        
        // Transmiting
        Label stateLabel3 = new Label("Tompo : " + station.Tomporisateur);  // vide au début
        stateLabel3.setLayoutX(circle.getCenterX() - 30 );
        stateLabel3.setLayoutY(circle.getCenterY() + 55); 
        stateLabel3.setStyle("-fx-font-size:12;-fx-text-fill: green;-fx-font-weight: bold;");
        stateLabel3.setVisible(true);
        station.setTransmmiting(stateLabel3);
        drawPanel.getChildren().add(stateLabel3);
        
        // WindowCentention
        Label stateLabel4 = new Label("CW : [ 0 : 7 ]");
        stateLabel4.setLayoutX(circle.getCenterX() - 30 );
        stateLabel4.setLayoutY(circle.getCenterY() + 40); 
        stateLabel4.setStyle("-fx-font-size:12;-fx-text-fill: black;-fx-font-weight: bold;");
        station.setWindowContention(stateLabel4);
        drawPanel.getChildren().add(stateLabel4);
        
        
        station.setDrawZone(drawPanel);
        
    }
    
    public void putTimer(SequentialTransition timer){
        this.timer = timer;
    }
    
    
    @FXML
    private void StartSimulationBtn(ActionEvent event){
        StartSimulation.setDisable(true);  // désactiver le botton
        StopSimulation.setDisable(false);
        System.out.println("startclicked");
          
        for (Station s : Stations){
            s.startTimer();
        }
        
        final var pause = new PauseTransition(new javafx.util.Duration(4000));
        pause.setOnFinished(evt -> suml());
        final var timer = new SequentialTransition(pause);
        timer.setCycleCount(PauseTransition.INDEFINITE);
        timer.play();
        putTimer(timer);
    }
    @FXML
    private void StopSimulation(ActionEvent event) {
        timer.pause();
        for(Station s: Stations){
            s.timer.pause();
        }
        StartSimulation.setDisable(false);
        StopSimulation.setDisable(true);
    }
    @FXML
    private void ResetBtn(ActionEvent event) {
        timer.pause();     
        drawPanel.getChildren().clear();
        Stations.clear();
        needSend.clear();
    }
    
    public void suml() {
        needSend.clear();
        for (Station s : Stations){
            // TODO:
            s.WindowContention.setText("CW : [ 0 : "+ s.Cw +" ]");
            //s.Readytosend.setText("Tompo : " + s.Tomporisateur);
            s.Transmmiting.setText("Tompo : " + s.Tomporisateur);
            if (0 == s.Tomporisateur){
                needSend.add(s);
            }
      }
        
        if(needSend.size() > 1){ // il y une collision
            for(Station s : needSend){
                s.NeedToSend.setText("Collision Dtected");
                //s.circle.setFill(Color.ROSYBROWN);
                //drawPanel.setStyle("-fx-background-color:rgba(255, 0, 0, 0.4);");
                s.circle.setStroke(Color.RED);
                s.missAjourBackoff();
            }
        }
        if(needSend.size() == 1){
            timer.pause();
            // RTS Sending
            needSend.get(0).Readytosend.setText("RTS Sending");
            needSend.get(0).circle.setStroke(Color.GREEN);
            // Searching destination
            int station = (int) (Math.random() * ( Stations.size()));
            while(needSend.get(0).id == Stations.get(station).id ){
                station = (int) (Math.random() * ( Stations.size()));
            }
            
            Stations.get(station).circle.setStroke(Color.AQUAMARINE);
            Stations.get(station).Readytosend.setText("RTS Reciving");
            Stations.get(station).pickStaions(Stations);
            Stations.get(station).setRtsSender(needSend.get(0));  // ce qui envoi le RTS
            for (Station s : Stations){
                if(s.id !=  needSend.get(0).id && s.id != Stations.get(station).id){
                    s.getTimer().pause();
                    s.circle.setStroke(Color.BURLYWOOD); // le RTS n'est pas a lui
                    s.Readytosend.setText("Nav Updated");
                }
            }
            
            needSend.get(0).CwMin = needSend.get(0).CwMinInitale;
            needSend.get(0).calculeBackof();
        }
    }
}
