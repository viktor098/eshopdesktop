/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andrusyak_shop;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Hlavní třída, která obsahuje metodu main, slouží k otevření hlavního okna
 * @author Andrusyak Viktor
 */
public class MainShop extends Application {
    
    /**
     * Metoda, která se zavolá při spuštění aplikace.
     * Obsahuje deklaraci a načtení nového okna.
     * @param primaryStage
     * @throws IOException 
     */
    @Override
    public void start(Stage primaryStage) throws IOException {     
        /**
         * Okno se načte ze souborů v projektu.
         * Automaticky zavolá controller.
         * Nastaví scénu pro hlavní okno.
         */
        Parent root = FXMLLoader.load(getClass().getResource("/andrusyak_shop/MainFXML.fxml"));       
        Scene scene = new Scene (root);      
        primaryStage.setScene(scene);
        
        /**
         * Stanovení minimalní velikosti hlavního okna.
         */
        primaryStage.setMinWidth(880);
        primaryStage.setMinHeight(440);
             
        primaryStage.show();     
    }

    /**
     * Main metoda. Zavolá FXML soubory (controllery).
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
