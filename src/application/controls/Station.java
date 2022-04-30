/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.controls;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author me_ba
 */
public class Station{
     
    // attr
    boolean firstEcoute = true;
    int CwMinInitale = 7;       //  la taille initiale de la fentere
    int CwMin = CwMinInitale;   //  la taille min de fenetre
    int Cw = CwMin;             // la taille de fenetre courant
    int CwMax = 128;            // la taile de fenetre max
    int iLimite = 5;            // seuil de transmission
    int i = 1;                  // nombre de tentative
    int TimeSlot = 500;          // timeSlot = 100us
    int SIFS = 1;              // SIFS = 10
    int DIFS = 2;  // DIFS
    int PIFS = SIFS + TimeSlot;        // PIFS
    int Tomporisateur = 0;                 // BackOff
    
    public int id;
    boolean veutTransmmtre = false;
    private boolean collision = false ;
    boolean Sending = false;
    int sendTimeRest=0;
    int ATTENDRE=0;
    boolean addDifs=false;
    boolean transmissionGood=true;
    Station receiver = null;
    boolean messageGenerated = false;
    private AnchorPane drawZone;
    Station RtsSender ;
    // graphics labels
    public Label Readytosend = new javafx.scene.control.Label();
    public Label NeedToSend = new javafx.scene.control.Label();
    public Label Transmmiting = new javafx.scene.control.Label();
    public Label WindowContention = new javafx.scene.control.Label();
    public ImageView image;
    Circle circle;
    List<Station> Stations = new ArrayList<>();
    
    
    
    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }
    
    SequentialTransition timer;

    public SequentialTransition getTimer() {
        return timer;
    }

    public void setRtsSender(Station RtsSender) {
        this.RtsSender = RtsSender;
    }
    
    // getterstateTransmisionLabel
    public Label getReadytosend() {
        return Readytosend;
    }
    // setterstateTransmision
    public void setReadytosend(Label Readytosend) {
        this.Readytosend = Readytosend;
    }
    // getterNeedToSend
    public Label getNeedToSend() {
        return NeedToSend;
    }
    // setterNeedToSend
    public void setNeedToSend(Label NeedToSend) {
        this.NeedToSend = NeedToSend;
    }
    // getterTransmmiting
    public Label getTransmmiting() {
        return Transmmiting;
    }
    // setterTransmmiting
    public void setTransmmiting(Label Transmmiting) {
        this.Transmmiting = Transmmiting;
    }
    // getterWindowContention
    public Label getWindowContention() {
        return WindowContention;
    }
    // setterWindowContention
    public void setWindowContention(Label WindowContention) {
        this.WindowContention = WindowContention;
    }
    
   
    Station (int id){  // le constructeur
        this.id = id;
        calculeBackof();
    }

    public int getTomporisateur() {
        return Tomporisateur;
    }

    public boolean isVeutTransmmtre() {
        return veutTransmmtre;
    }
    
    public boolean isSending() {
        return Sending;
    }
  
    
    public void calculeBackof(){
        Random rand = new Random();
        this.veutTransmmtre = rand.nextBoolean(); // if is it trueStation need to send data else not
        this.Tomporisateur =  (int) ((Math.random() * (Cw + 1) + 1));
    }
    
        public void changeEtatdeTransmmtre(){
        Random rand = new Random();
        this.veutTransmmtre = rand.nextBoolean();
    }
        
    public void missAjourBackoff(){
        i++;
        if(CwMin < CwMax) {
            CwMin = (int) Math.pow(2, 2 + i) - 1;
            Cw = CwMin;
            calculeBackof();
        } else {
            calculeBackof();
        }
    }
    
    public void startTimer() {
        final var pause = new PauseTransition(new javafx.util.Duration(3000));
        pause.setOnFinished(evt -> propertyChange());
        final var timer = new SequentialTransition(pause);
        timer.setCycleCount(PauseTransition.INDEFINITE);
        timer.play();
        this.timer = timer;
    }
    
    
    
    public void setCollision(boolean collision) {
        this.collision = collision;
    }
    
    public boolean isCollision() {
           return collision;
    }
    
        public void setDrawZone(AnchorPane drawZone) {
        this.drawZone = drawZone;
    }

    public void animationCircle(){
        for  (int k = 0; k < 101; k++){
            circle.setStroke(Color.WHITE);
            circle.setRadius(k);
            circle.setStroke(Color.GREEN);
        }
        circle.setStroke(Color.BLACK);
    }

    public void propertyChange() {
        
       /////////////// 
       if(RtsSender != null && RtsSender.Readytosend.getText() == "Ack reciving")
       {   
           RtsSender.CwMin = RtsSender.CwMinInitale;
           RtsSender.i = 1;
           circle.setStroke(Color.BLACK);
           Readytosend.setText("");
            SimulationMakerController.timer.play();
            for(Station s :Stations){
                s.getTimer().play();
            }
            RtsSender.circle.setStroke(Color.BLACK);
            RtsSender.Readytosend.setText("");
            RtsSender = null;
            return;
       }
       if(RtsSender != null && Readytosend.getText() == "Reciving Data"){
           
           NeedToSend.setText("");
           Readytosend.setText("Sending ack");
           RtsSender.Readytosend.setText("Ack reciving");
           return;
       }
        /////////////
        if (RtsSender != null && RtsSender.Readytosend.getText() == "Sending")
        {
            ////////////////////////////////////
            Readytosend.setText("Reciving Data");
            NeedToSend.setText("Waiting SIFS");
            RtsSender.Readytosend.setText("");
            return;
            //////////////////////////////////
            /*circle.setStroke(Color.BLACK);
            SimulationMakerController.timer.play();
            for(Station s :Stations){
                s.getTimer().play();
            }
            RtsSender.circle.setStroke(Color.BLACK);
            RtsSender.Readytosend.setText("");
            RtsSender = null;
            return;*/
        }
        if(RtsSender != null && RtsSender.Readytosend.getText() == "CTS Reciving"){
            RtsSender.Readytosend.setText("Sending");
            
            Readytosend.setText("");
            return;
        }
        if(Readytosend.getText() == "Nav Updated"){
            circle.setStroke(Color.BLACK);
            Readytosend.setText("");
        }
        
        if(Readytosend.getText() == "RTS Reciving"){
            RtsSender.Readytosend.setText("CTS Reciving");
            // TODO      
            Readytosend.setText("CTS Sending");
            return;
        }
        if(Tomporisateur != 0)
                Tomporisateur --;

        if(Readytosend.getText() == "Sendeing"){
            Readytosend.setText("");
            circle.setStroke(Color.BLACK);
            //circle.setFill(Color.TRANSPARENT);
        }
        if(NeedToSend.getText() == "Collision Dtected"){
            NeedToSend.setText("");
            circle.setStroke(Color.BLACK);

        }
        if(circle.getStroke() == Color.AQUAMARINE){
            circle.setStroke(Color.BLACK);
            Readytosend.setText("");
        }
            
    }
    
    public void pickStaions(List<Station> s){
        this.Stations = s;
    }
}
