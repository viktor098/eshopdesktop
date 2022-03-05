/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andrusyak_shop;

import static andrusyak_shop.MainFXMLController.current_product;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller, zodpovědný za okno vytvoření nového produktu.
 *
 * @author Andrusyak Viktor
 */
public class NewProductController extends MainFXMLController implements Initializable {

    /** Hlavní Layouty, obsahujicí další prvky okna. */
    @FXML
    private VBox productBox;
    @FXML
    private ImageView imageView;
    

    /** Deklarace všech Inputů, potřebných pro vytvoření produktu. */

    @FXML
    private TextField new_name, new_category, new_shortDesc, new_longDesc,
            new_price, new_amount;

    /** Proměnna pro posílání obrázku do databáze. */
    byte[] imageBytes;

    /**
     * Metoda načte informace z inputů a pošle na server pro vytvoření produktu.
     * @param event 
     */
    @FXML
    void createProduct(ActionEvent event) {
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){    
            out.println("createProduct");
                
            String [] newUser = {
                new_name.getText(), new_category.getText(), new_shortDesc.getText(), 
                new_longDesc.getText(), new_price.getText(), new_amount.getText()
            };
            
            ObjectOutputStream newProductStream = new ObjectOutputStream(socket.getOutputStream());
           
            newProductStream.writeObject(newUser);
            newProductStream.writeInt(imageBytes.length);
            newProductStream.write(imageBytes);
          
            Stage current_stage = (Stage) new_name.getScene().getWindow();
            current_stage.close();

        } catch (IOException ex) {
            errorAlert("Nelze se připojít k serveru. Opravte připojení.");
            
            Stage current_stage = (Stage) new_name.getScene().getWindow();
            current_stage.close();
        } 
    }
    
    /**
     * Metoda, která umožní vybrat obrázek z úložiště a načte ho do paměti.
     * @param event
     * @throws FileNotFoundException
     * @throws IOException 
     */
    @FXML
    void selectImage(MouseEvent event) throws FileNotFoundException, IOException {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vyberte, jaký obrázek chcete nahrát");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            imageBytes = Files.readAllBytes(file.toPath());               
        } 
    }
    
    
    /**
     * Metoda inicializuje potřebné prvky pro zadávání údajů o produktu.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!addNewProduct){
            productBox.getChildren().clear();
            
            /** Obrázek. */
            ImageView imageView = new ImageView();
            imageView.setFitWidth(400);
            imageView.setFitHeight(300);
            VBox.setMargin(imageView, new Insets(15, 0, 10, 0));
            
            /** Název produktu. */
            Text product_name = new Text(current_product[1]);
            product_name.setStyle("-fx-font: 24 system;");
            VBox.setMargin(product_name, new Insets(0, 0, 0, 15));
            
            HBox hBox1 = new HBox();
            hBox1.setAlignment(Pos.BASELINE_CENTER);
            
            /** Popis produktu. */
            Text description = new Text("Popis:"); 
            description.setStyle("-fx-font: 15 system;");
            
            /** Dlouhý popis produktu */
            Text all_description = new Text(current_product[4]);
            HBox.setMargin(all_description, new Insets(0, 0, 0, 10));
            all_description.setWrappingWidth(320);
            all_description.setStyle("-fx-font: 14 system;");
            
            hBox1.getChildren().addAll(description, all_description);
            
            VBox spacing1 = new VBox();
            VBox.setVgrow(spacing1, Priority.ALWAYS);
            
            HBox hBox2 = new HBox();
            hBox2.setAlignment(Pos.BOTTOM_CENTER);
            VBox.setMargin(hBox2, new Insets(0, 15, 10, 15));
            
            /** Cena produktu */
            Text product_price = new Text("Cena: " + current_product[6] + ",-"); 
            product_price.setStyle("-fx-font: 25 system;");

            HBox spacing2 = new HBox();
            HBox.setHgrow(spacing2, Priority.ALWAYS);

            /** Tlačitko. */
            Button toCard = new Button("Přidat");
            toCard.setStyle("-fx-focus-color: BLACK; -fx-font: 15 system;");
         
        
            hBox2.getChildren().addAll(product_price, spacing2, toCard);
            
            productBox.getChildren().addAll(imageView, product_name, hBox1, spacing1, hBox2); 
        }
    
    }    
    
}
