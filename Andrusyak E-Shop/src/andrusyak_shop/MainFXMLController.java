/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andrusyak_shop;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller pro hlavní okno.
 *
 * @author Andrusyak Viktor
 */
public class MainFXMLController implements Initializable {


    /** Inicializace čísla portu a adresy serveru pro připojení. */
    int portNumber = 8888;
    String inetAddress = "192.168.56.1";
    
    /**
    * Údaje o současném uživateli.
    * Pokud ID = 0, uživatel není přihlašený.
    * Permission 0 = normální užibvatel, 1 = admin práva.
    */
    static int user_ID = 0;
    static int permission = 0;                                                          //OPRÁVNĚNÍ
    static String user_first_name;
    static String user_last_name;
     
    /** Určuje jestli uživatel se přihlásil. */
    static boolean isLogged = false;
    
    /** Pomáhá při vypisování tlačítka "přidat nový produkt". */
    static boolean addNewProduct = false;
    
    /** Pomáhá při zpracování současného produktu. */
    static String[] current_product;
    
    /** Array, obsahující všechny produkty, které se načetly z databáze. */
    static ArrayList<String[]> allProducts;
    
    /** Obsahuje produkty, přidíné do košíku. */
    static ArrayList<String[]> myCard;
    
    /** Pomocný String pro seřazení kategorii. */
    String current_category = "All";
    
    /** Layout, obsahující kategorie. */
    @FXML
    private VBox categories;

    /** Layout, obsahující produkty. */
    @FXML
    private FlowPane flowPane;
    
    /** Layout pro grafické potřeby. */
    @FXML
    private ScrollPane scrollPane;
    
    /** Box pro seřazení produktů. */ 
    @FXML
    private ChoiceBox<String> choiceBox;
    
    /** Obrázek pro přihlašení / odhlašení. */
    @FXML
    private ImageView sign_icons;
    
    /** Vstupní okno pro vyhledávání. */
    @FXML
    private TextField search_field;
    
    /** Číslo nahoře vpravo, označujicí počet položek v košíku */
    @FXML
    private Text cardCount;
    
    /** Počet položek v košíku */ 
    static int cardCountNumber = 0;
    
    /** Text nahoře (může být buď Přihlášení nebo jméno uživatele) */
    @FXML
    Text login;
    
    
    
    

    /**
     * Metoda pošle na server ID uživatele a ID produktu(parametr), přidaného do košíku.
     * @param product_ID 
     */
    private void requestToCard (String product_ID){
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){    

            out.println("toCard");
    
            out.println(product_ID);
            out.write(user_ID);

        } catch (IOException ex) {
            errorAlert("Nelze se připojít k serveru. Opravte připojení.");
        }
    }
    
    /**
     * Metoda, která zkontroluje jestli nějaký produkt je v košíku.
     * @param product
     * @return 
     */
    private boolean checkCard(String[] product) {
        for (String[] item : myCard){
            if (product[0].equals(item[0])) return true;          
        }
        return false;
    }
    
    /**
     * Vypiše do hlavního Layoutu jeden konkrétní produkt.
     * Každý prvek dostává určité grafické úpravy.
     * @param product 
     */
    private void addProduct(String[] product){
        
        /** Layout, obsahujicé informace o produktu. */
        VBox vBox = new VBox();
        vBox.setPrefWidth(200);
        vBox.setPrefHeight(300);
        vBox.setMaxWidth(200);
        vBox.setMaxHeight(300);
        vBox.setStyle("-fx-background-color:WHITE; -fx-border-color:BLACK;");
        
        flowPane.getChildren().add(vBox);
        
        /** Obrázek, momentálně se nevypisuje. */
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        VBox.setMargin(imageView, new Insets(10, 0, 5, 0));
        
        /** Název produktu. */
        Text product_name = new Text(product[1]);
        product_name.setCursor(Cursor.HAND);
        /** Po kliknutí na název otevře větší okno s delším popisem produktu. */
        product_name.setOnMouseClicked(e -> {
            current_product = product;
            openProduct();
        });
        product_name.setStyle("-fx-font: 19 system;");
        VBox.setMargin(product_name, new Insets(0, 0, 0, 10));
        
        /** Popis produktu. */
        Text product_description = new Text(product[3]);
        product_description.setStyle("-fx-fill: GRAY;");
        product_description.setWrappingWidth(178);
        VBox.setMargin(product_description, new Insets(0, 0, 0, 10));
        
        VBox spacing1 = new VBox();
        VBox.setVgrow(spacing1, Priority.ALWAYS);
        
        HBox hBox = new HBox();
        VBox.setMargin(hBox, new Insets(0, 10, 5, 10));
        
        vBox.getChildren().addAll(imageView, product_name, product_description, 
                spacing1, hBox);
        
        /** Cena produktu */
        Text product_price = new Text(product[6] + ",-");
        product_price.setStyle("-fx-font: 19 system;");
        
        HBox spacing2 = new HBox();
        HBox.setHgrow(spacing2, Priority.ALWAYS);
        
        
        /** Tlačítko pro přidání do košíku. */ 
        Button toCardBtn = new Button("do košíku");
        toCardBtn.setStyle("-fx-focus-color: BLACK;");
        
        if(checkCard(product)) {
            toCardBtn.setText("přídáno");
            toCardBtn.setDisable(true);
        }
        
        /**
         * Po stusnutí tlačítka "do košíku" provede přidábí a poku uživatel 
         * je přihlašený, pošle přidábí na server
         */
        toCardBtn.setOnAction(e -> {
            myCard.add(product);
            cardCountNumber++;
            cardCount.setText(String.valueOf(cardCountNumber));  
            if (user_ID != 0) requestToCard(product[0]);
            toCardBtn.setText("přídáno");
            toCardBtn.setDisable(true);
        });
         
        hBox.getChildren().addAll(product_price, spacing2, toCardBtn);
        
        /** Pokud přihlašený uživatel je admin, vypiše se tlačítko pro editací */
        if (permission == 1){
            Button edit = new Button("+");
            edit.setStyle("-fx-focus-color: BLACK;");
            HBox.setMargin(edit, new Insets(0, 0, 0, 5));
            edit.setOnAction(e -> {   
                setCount(product);   
            });
            hBox.getChildren().add(edit);
        }
        
        /**
         * Vypisuje kategorii.
         * Každý produkt má svoji kategorii, která se vypiše jednou.
         */
        boolean isCategory = false;
        for (Node node : categories.getChildren()){
            if (((Text)node).getText().equals(product[2])) isCategory = true;
        } 
        if (!isCategory) {      
            Text category = new Text();
            category.setStyle("-fx-font: 20 system;");
            category.setText(product[2]);
            VBox.setMargin(category, new Insets(10, 0, 0, 15));
            
            category.setCursor(Cursor.HAND);
            category.setOnMouseClicked(e ->{
            choiceBox.getSelectionModel().clearSelection();
            flowPane.getChildren().clear();
            current_category = category.getText();       
                for (String [] item : allProducts){
                    if (item[2].equals(current_category)){
                        addProduct(item);
                    }
                }
                addNewProductButton();
            });
            categories.getChildren().add(category);
        }     
    }
    
    /**
     * Provede se pří kliknutí na tlačítko editace.
     * Ukaže kolik je současně ve skladu produktu.
     * Umožní dodat zboží a pošle to na server.
     * @param product 
     */
    private void setCount(String[] product){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Dodání zboží");
        dialog.setHeaderText(null);
        dialog.setContentText("Ve skladu jsou " + product[7] + " kusů. Kolik chcete přidat?");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            int addCount = Integer.parseInt(result.get());
            addCount += Integer.parseInt(product[7]);
            
            try (  
                Socket socket = new Socket(inetAddress, portNumber);    
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
            ){    
                out.println("addCount");

                out.println(product[0]); // ID
                out.write(addCount); // počet
                
            } catch (IOException ex) {
                errorAlert("Nelze se připojít k serveru. Opravte připojení.");
            }  
        }
    }
    
    
    
    /**
     * Metoda pro vytvoření nového tlačitka na dodání zboží.
     */
    private void addNewProductButton(){
        if (permission == 1){
            Button newProduct = new Button("+");
            newProduct.setStyle("-fx-font: 40 system;");
            newProduct.setOnAction(e -> {   
                addNewProduct = true;
                openProduct();   
                addNewProduct = false;
            });
            flowPane.getChildren().add(newProduct);
        }
    }
    
    /**
     * Metoda pro otevření větčího okna produktu s dlouhým popisem.
     */
    private void openProduct () {
        Parent aut_root;
        try {
            aut_root = FXMLLoader.load(getClass().getResource("/andrusyak_shop/NewProduct.fxml"));
            Stage aut_stage = new Stage();
            if (addNewProduct) aut_stage.setTitle("Přidat nový produkt");
            else  aut_stage.setTitle("Podrobnosti produktu");       
            aut_stage.setScene(new Scene(aut_root));
            aut_stage.initModality(Modality.APPLICATION_MODAL);
            aut_stage.setResizable(false);
            aut_stage.showAndWait();
        } catch (IOException ex) {
            errorAlert("Nelze otevřit nové okno.");
        }
    }
    
    /**
     * Otevření Alertu, který zobrazí chybovou hlášku.
     * Parametr vypisuje v okně.
     * @param errorInfo 
     */
    static void errorAlert(String errorInfo){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chyba");
        alert.setHeaderText(null);
        alert.setContentText(errorInfo);
        alert.showAndWait();
    }
    
    /**
     * Otevření informačního Alertu.
     * Parametr vypisuje v okně.
     * @param info 
     */
    static void infoAlert (String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Oznámení");
        alert.setHeaderText(null);
        alert.setContentText(info);
        alert.showAndWait();
    }
    
   /**
    * Spojí se se Serverem a načte všechny produkty (pokud vstupní argument bude
    * "listOfProducts".
    * @param request
    * @throws ClassNotFoundException 
    */
    private void requestToServer(String request) throws ClassNotFoundException{
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){    
            out.println(request);
    
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); 
                    
            while (true){          
                Object result = in.readObject();
                
                if (result instanceof String){
                    if (((String)result).equalsIgnoreCase("quit")) {
                        break;
                    }
                }
                              
                String[] resultList =(String[]) result;
                allProducts.add(resultList);                        
            }
            
        } catch (IOException ex) {
            errorAlert("Nelze se připojít k serveru. Opravte připojení.");
        }
    }
    
    /**
     * Metoda pro kontrolu uživatele, umožní přejít do okna přihlášení.
     * Provede se po stisknutí textu "Přihlášení".
     * @param event
     * @throws IOException 
     */
    @FXML
    void user_control(MouseEvent event) throws IOException {
        Parent aut_root;
        
        aut_root = FXMLLoader.load(getClass().getResource("/andrusyak_shop/Authentication.fxml"));
        Stage aut_stage = new Stage();
        aut_stage.setTitle("Přihlašení");
        aut_stage.setScene(new Scene(aut_root));
        aut_stage.initModality(Modality.APPLICATION_MODAL);
        aut_stage.setResizable(false);
        
        aut_stage.showAndWait();
            
        if (isLogged){
            File file = new File("src/andrusyak_shop/logout_white.png");
            Image image = new Image(file.toURI().toString());
            sign_icons.setImage(image);
            login.setText(user_last_name + " " + user_first_name);
            cardCount.setText(String.valueOf(cardCountNumber));
        }  
        
        allProducts(event);     
    }
    
    /**
     * Metoda pro seřazení produktú.
     * Je spojená pomocí parametru s vybránou hodnotou v Boxu pro seřazení.
     * @param value 
     */
    private void sortProducts(int value){        
        flowPane.getChildren().clear();  
        
        ArrayList<String[]> categoryProducts = new ArrayList<>();  
        
        boolean isCategory  = false;
        for (String [] item : allProducts){
            if (item[2].equals(current_category)){
                categoryProducts.add(item);
                isCategory = true;
            }
        } 
        
        if (!isCategory) categoryProducts = allProducts;
        
        Collections.sort(categoryProducts, new Comparator<>(){
            @Override
            public int compare (String[] strings, String [] otherStrings){                 
                int first = Integer.parseInt(strings[6]);
                int second = Integer.parseInt(otherStrings[6]);

                if (value == 0){
                    if (first > second) return 1;
                    else if (first < second) return -1;
                    else return 0;
                } else {
                    if (first < second) return 1;
                    else if (first > second) return -1;
                    else return 0;
                }
            }
        });

        for (String[] product : categoryProducts){
            addProduct(product);
        }  
        addNewProductButton();
    }
    
    /**
     * Metoda se provede po stusknutí obrázku vedle Přihlášení.
     * Funguje jako Texxt vlevo, ale po přihlášní změní obrázek a umožní
     * uživateli se odhlásit.
     * @param event
     * @throws IOException 
     */
    @FXML
    void sign_icon(MouseEvent event) throws IOException {
        
        if (!isLogged){
            user_control(event);
        } 
        // odhlašení
        else {
            user_ID = 0;
            permission = 0;
            cardCountNumber = 0;
            user_first_name = null;
            user_last_name = null;   
            isLogged = false;  
            myCard.clear();
            cardCount.setText(String.valueOf(cardCountNumber));
            allProducts(event);
            File file = new File("src/andrusyak_shop/padlock_white.png");
            Image image = new Image(file.toURI().toString());
            sign_icons.setImage(image);
            login.setText("Přihlášení");  
            infoAlert("Uživatel byl odhlášen.");   
        }
    }
    
    /**
     * Vypiše všechny produkty.
     * Provede se po stisknutí textu "E-Shop".
     * @param event 
     */
    @FXML
    void allProducts(MouseEvent event) {
        choiceBox.getSelectionModel().select("od nejdražšího"); 
        current_category = "All";
        flowPane.getChildren().clear();
        for (String [] item : allProducts){
            addProduct(item);
        }
        addNewProductButton();
    }
     
    /**
     * Metoda umožní přejít do okna košíku.
     * @param event
     * @throws IOException 
     */
    @FXML
    void toCart(MouseEvent event) throws IOException {
        if (!myCard.isEmpty()){
            Parent aut_root;

            aut_root = FXMLLoader.load(getClass().getResource("/andrusyak_shop/CartFXML.fxml"));
            Stage aut_stage = new Stage();
            aut_stage.setTitle("Košík");
            aut_stage.setScene(new Scene(aut_root));
            aut_stage.initModality(Modality.APPLICATION_MODAL);
            aut_stage.setResizable(false);

            aut_stage.showAndWait();

            cardCount.setText(String.valueOf(cardCountNumber)); 
            sortProducts(choiceBox.getSelectionModel().getSelectedIndex());
        }
    }


    /**
     * Metoda se spustí po otevření okna, za které je zodpovědný controller.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {   
        
        allProducts = new ArrayList<>();
        myCard = new ArrayList<>();
        
        /** Nastaví zalomování produktů podle velikosti okna */
        flowPane.prefWrapLengthProperty().bind(scrollPane.widthProperty());
        
        /** Přidá do choiceBoxu položky a defaultně vybere jednu z nich */
        choiceBox.getItems().addAll("od nejlevnějšího", "od nejdražšího");
        choiceBox.getSelectionModel().select("od nejlevnějšího");      
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(
         (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            sortProducts(new_val.intValue());       
        });
        
        /**
         * Vyhledávání produktů (při změně písmena projde všechny produkty).
         */
        search_field.textProperty().addListener((observable, oldValue, newValue) -> {
            flowPane.getChildren().clear();
            if (!newValue.equals("")){                
                for (String[] product : allProducts){
                    if (product[1].toLowerCase().contains(newValue.toLowerCase())){
                        addProduct(product);
                    }
                }
            } else {
                for (String[] product : allProducts){
                    addProduct(product);
                }
            }
        });
        

        try {
            requestToServer("listOfProducts");
        } catch (ClassNotFoundException ex) {
            errorAlert("Ze serveru nepřišla žádná odpověď.");
        }
        
        sortProducts(choiceBox.getSelectionModel().getSelectedIndex());

    }    
    
}
