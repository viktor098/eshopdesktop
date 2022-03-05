/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andrusyak_shop;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller, zodpovědný okno košíku, zadavání doručovácích údajů a platební brány.
 *
 * @author Andrusyak Viktor
 */
public class CartFXMLController extends MainFXMLController implements Initializable {

    /** Hlavní layout, obsahujicí další prvky. */
    @FXML
    private FlowPane flowPane;
    
    /** Deklarace Inputů pro doručovácí údaje. */
    @FXML
    private Text first_text, second_text, user_name, del_address, del_city, 
            del_postcode, totalPrice;

    /** Celková cena košíku. */
    int orderPrice = 0;
    
    /** kompletně informace o doručení. */
    String[] deliveryInfo;
    
    /** uživatel pro doručení. */
    String[] user;
    
    /** Deklarace spinnerů pro zadávání potřebného množství produktů.*/
    ArrayList<Spinner> priceList;
    
    /** Booleany, určujicí, jestli program může postupně pokračovat v nákupu. */
    boolean hasDelInfo = false;
    boolean orderIsFinished = false;
    
    
    /**
     * Metoda vypiše všechny položky z košíku.
     * Obsahuje také Spinner, umožňujicí zadat množství (maximalní hodnotu má podle
     * toho, kolik je produktu ve skladu) a tlačitko na odebrání produktu z košíku.
     */
    private void allItems(){
        orderPrice = 0;
        priceList = new ArrayList<>();
        flowPane.getChildren().clear();
        for (String [] item : myCard){
            
            HBox hBox = new HBox();
            hBox.setPrefWidth(425);
            hBox.setPrefHeight(70);
            hBox.setAlignment(Pos.CENTER_LEFT);
            VBox.setMargin(hBox, new Insets(5, 0, 0, 0));

            hBox.setStyle("-fx-background-color:WHITE; -fx-border-color:GRAY;");

            flowPane.getChildren().add(hBox);

            ImageView imageView = new ImageView(); 
            imageView.setFitWidth(60);
            imageView.setFitHeight(50);
            VBox.setMargin(imageView, new Insets(0, 0, 0, 10));

            Text product_name = new Text(item[1]); 
            product_name.setStyle("-fx-font: 19 system;");
            product_name.setWrappingWidth(100);
            VBox.setMargin(product_name, new Insets(0, 0, 0, 5));
        
            VBox spacing = new VBox();
            HBox.setHgrow(spacing, Priority.ALWAYS);
        
            VBox vBox1 = new VBox();
            vBox1.setPrefWidth(100);
            vBox1.setAlignment(Pos.CENTER);
            vBox1.setStyle("-fx-border-width: 0 1 0 1; -fx-border-color: GRAY");
            
            Text price1 = new Text("Cena za kus");                                         
            price1.setStyle("-fx-font: 14 system;");
        
            Text price2 = new Text(item[6] + ",-");                                       
            price2.setStyle("-fx-font: 21 system;");
        
            vBox1.getChildren().addAll(price1, price2);
            
            VBox vBox2 = new VBox();
            vBox2.setAlignment(Pos.CENTER);
            HBox.setMargin(vBox2, new Insets(0, 20, 0, 20));
            
            Text count1 = new Text("Počet");                                         
            count1.setStyle("-fx-font: 13 system;");

            
            Spinner spinner = new Spinner(1, Integer.parseInt(item[7]), 1);
            priceList.add(spinner);
            orderPrice += (1 * Integer.parseInt(item[6]));
            totalPrice.setText(orderPrice + " Kč");
            spinner.setPrefWidth(50);
            spinner.setPrefHeight(30);
            
            spinner.valueProperty().addListener((obs, oldValue, newValue) -> { 
                orderPrice -= (((Integer) oldValue) * Integer.parseInt(item[6]));
                orderPrice += (((Integer) newValue) * Integer.parseInt(item[6]));
                totalPrice.setText(orderPrice + " Kč");   
            });

            vBox2.getChildren().add(spinner);
            
            Button removeItem = new Button("X");
            removeItem.setStyle("-fx-font: 14 system;");
            HBox.setMargin(removeItem, new Insets(0, 10, 0, 0));
            removeItem.setOnAction(e -> {
             
                if (user_ID != 0){
                    cleanCart(item[0]);
                }

                myCard.remove(item);
                cardCountNumber--;
                allItems();
                if (myCard.isEmpty()){
                    Stage stage = (Stage) flowPane.getScene().getWindow();
                    stage.close();
                }

            });
            
            hBox.getChildren().addAll(imageView, product_name, spacing, vBox1, 
                    vBox2, removeItem);
            
        }
    }
    
    
    /**
     * Metoda získá ze serveru informace o uživateli.
     * @throws ClassNotFoundException 
     */
    private void getUserInfo() throws ClassNotFoundException{
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){    

            out.println("getUserInfo");
            
            out.println(user_ID);

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); 
             
            Object result = in.readObject();

            user =(String[]) result;
            
            user_name.setText(user[1]);
            del_address.setText(user[5]+", "+user[6]);
            del_city.setText(user[7]);
            del_postcode.setText(user[8]);
       
        } catch (IOException ex) {
            errorAlert("Nelze se připojít k serveru. Opravte připojení.");
        }  
    }
    
    /**
     * Metoda, která pošle údaje pro novou objednávku na server.
     * @param orderInfo 
     */
    private void createOrder(String[] orderInfo){
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){    
            out.println("createOrder");
                         
            ObjectOutputStream newOrderStream = new ObjectOutputStream(socket.getOutputStream());
           
            newOrderStream.writeObject(orderInfo);
              
            for (String[] item : myCard){
                newOrderStream.writeObject(item); 
                int amount =(Integer) priceList.get(myCard.indexOf(item)).getValue();
                newOrderStream.writeInt(amount);
                newOrderStream.flush();
            }
            
            orderIsFinished = true;
            
            if (user_ID != 0) cleanCart("all");
                       
        } catch (IOException ex) {
            errorAlert("Nelze se připojít k serveru. Opravte připojení.");       
        }     
    }
    
    
    /**
     * Metoda validuje číslo karty s pomocí algoritmu.
     * @param cardNumber
     * @return 
     */
    private static boolean cardIsValid(String cardNumber) {
        int[] ints = new int[cardNumber.length()];
        for (int i = 0; i < cardNumber.length(); i++) {
                ints[i] = Integer.parseInt(cardNumber.substring(i, i + 1));
        }
        for (int i = ints.length - 2; i >= 0; i = i - 2) {
                int j = ints[i];
                j = j * 2;
                if (j > 9) {
                        j = j % 10 + 1;
                }
                ints[i] = j;
        }
        int sum = 0;
        for (int i = 0; i < ints.length; i++) {
                sum += ints[i];
        }
        if (sum % 10 == 0) return true;
        else return false;
    }
    
    /**
     * Metoda vykreslí potřebné prvky pro platební bránu.
     */
    private void payGateway(){
        Stage gateway = new Stage();
        gateway.setTitle("Platební brána");
        gateway.initModality(Modality.APPLICATION_MODAL);
        gateway.setResizable(false);
        

        VBox root = new VBox();
        root.setStyle("-fx-background-color: WHITE;");
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 300);
        
        Text cardText = new Text("Zadejte údaje karty");
        VBox.setMargin(cardText, new Insets(0, 0, 30, 0));
        cardText.setStyle("-fx-font: 18 system;");
        
        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        Text cardNumberText = new Text("Číslo karty: ");
        TextField cardNumber = new TextField(); 
        cardNumber.setPrefWidth(120);
        
        cardNumber.textProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, 
                String newValue) {
                if (!newValue.matches("\\d*")) {
                    cardNumber.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        hBox1.getChildren().addAll(cardNumberText, cardNumber);
        
        
        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox2, new Insets(20, 0, 30, 0));
        Text cardDateText = new Text("Platnost do: ");
        TextField cardDate = new TextField();
        cardDate.setPrefWidth(50);
        Text cardVerifyText = new Text("Ověřovácí kód: ");
        HBox.setMargin(cardVerifyText, new Insets(0, 0, 0, 30));
        TextField cardVerify = new TextField();   
        cardVerify.setPrefWidth(35);
        hBox2.getChildren().addAll(cardDateText, cardDate, cardVerifyText, cardVerify);
        
        Button finishOrder = new Button("Zaplatit " + totalPrice.getText());
        finishOrder.setStyle("-fx-font: 16 system;");
        finishOrder.setOnAction(e -> {
            
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/yy");
            simpleDateFormat.setLenient(false);
            Date expiry;
            try {
                expiry = simpleDateFormat.parse(cardDate.getText());
                boolean expired = expiry.before(new Date());
                if (expired){
                    errorAlert("Platnost karty již vypršela.");
                } else {      
                    if (!cardIsValid(cardNumber.getText())) {
                            errorAlert("Číslo karty není správné.");
                    } else {
                        Stage current_stage = (Stage) finishOrder.getScene().getWindow();

                        if (user_ID != 0) {                
                            createOrder(user);
                        }  
                        else createOrder(deliveryInfo);

                        current_stage.close();     
                    }    
                }
            } catch (ParseException ex) {
                errorAlert("Zadejte správný datum platnosti karty.");
            }
            
        });
        
        
        root.getChildren().addAll(cardText, hBox1, hBox2, finishOrder);
        
        
        gateway.setScene(scene);
        gateway.showAndWait();     
    }
    
    /**
     * Metoda vykreslí prvky pro zadávání doručovácích údajů.
     */
    private void deliveryInfo() {
        
        Stage delivery = new Stage();
        delivery.setTitle("Doručovácí údaje");
        delivery.initModality(Modality.APPLICATION_MODAL);
        delivery.setResizable(false);
        

        VBox root = new VBox();
        root.setStyle("-fx-background-color: WHITE;");
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 500);
        
        Text deliveryText = new Text("Zadejte svoje údaje");
        VBox.setMargin(deliveryText, new Insets(0, 0, 20, 0));
        deliveryText.setStyle("-fx-font: 18 system;");
        
        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox1, new Insets(0, 0, 5, 0));
        Text lastNameText = new Text("Příjmení: ");
        TextField lastName = new TextField(); 
        lastName.setPrefWidth(120);
        hBox1.getChildren().addAll(lastNameText, lastName);
        
        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox2, new Insets(0, 0, 5, 0));
        Text firstNameText = new Text("Jméno: ");
        TextField firstName = new TextField(); 
        firstName.setPrefWidth(110);
        hBox2.getChildren().addAll(firstNameText, firstName);
        
        HBox hBox3 = new HBox();
        hBox3.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox3, new Insets(0, 0, 5, 0));
        Text emailText = new Text("Email: ");
        TextField email = new TextField(); 
        email.setPrefWidth(100);
        hBox3.getChildren().addAll(emailText, email);
        
        HBox hBox4 = new HBox();
        hBox4.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox4, new Insets(0, 0, 5, 0));
        Text mobileText = new Text("Telefon: ");
        TextField mobile = new TextField(); 
        mobile.setPrefWidth(120);
        hBox4.getChildren().addAll(mobileText, mobile);
        
        HBox hBox5 = new HBox();
        hBox5.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox5, new Insets(0, 0, 5, 0));
        Text streetText = new Text("Ulice: ");
        TextField street = new TextField(); 
        street.setPrefWidth(140);
        hBox5.getChildren().addAll(streetText, street);
        
        HBox hBox6 = new HBox();
        hBox6.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox6, new Insets(0, 0, 5, 0));
        Text houseText = new Text("Číslo popisné: ");
        TextField house = new TextField(); 
        house.setPrefWidth(80);
        hBox6.getChildren().addAll(houseText, house);
        
        HBox hBox7 = new HBox();
        hBox7.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox7, new Insets(0, 0, 5, 0));
        Text cityText = new Text("Město: ");
        TextField city = new TextField(); 
        city.setPrefWidth(140);
        hBox7.getChildren().addAll(cityText, city);
        
        HBox hBox8 = new HBox();
        hBox8.setAlignment(Pos.CENTER);
        VBox.setMargin(hBox8, new Insets(0, 0, 25, 0));
        Text postcodeText = new Text("PSČ: ");
        TextField postcode = new TextField(); 
        postcode.setPrefWidth(80);
        hBox8.getChildren().addAll(postcodeText, postcode);
        
        
        Button continueButton = new Button("Pokračovat k platební bráně");
        continueButton.setStyle("-fx-font: 16 system;");
        continueButton.setOnAction(e -> {
            
            deliveryInfo = new String[]{"0", lastName.getText(), firstName.getText(), email.getText(),
                mobile.getText(), street.getText(), house.getText(), city.getText(), postcode.getText()};

            hasDelInfo = true;
            
            Stage current_stage = (Stage) deliveryText.getScene().getWindow();
            current_stage.close();
            
            
        });
                
        root.getChildren().addAll(deliveryText, hBox1, hBox2, hBox3, hBox4,
                hBox5, hBox6, hBox7, hBox8, continueButton);
        
        
        delivery.setScene(scene);
        delivery.showAndWait();
        
        
    }
    
    /**
     * Metoda pošle na server potřebné údaje k vyčištění košíku u uživatele.
     * @param description 
     */
    private void cleanCart(String description){
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){    

            out.println("cleanCart");
    
            out.println(user_ID);
            
            out.println(description);
                   
        } catch (IOException ex) {
            errorAlert("Nelze se připojít k serveru. Opravte připojení.");       
        }    
    }
    
    
    /**
     * Metoda se spustí po stusknutí tlačitka "Zaplatit".
     * Používá booleany a postupuje až k dokončení nákupu.
     * @param event 
     */
    @FXML
    void buy(ActionEvent event) {
        
        if (user_ID == 0) deliveryInfo();
        else hasDelInfo = true;
        if (hasDelInfo) payGateway();
        if (orderIsFinished) {
            Iterator<String[]> item = myCard.iterator();
            while (item.hasNext()) {
               String[] s = item.next();
               cardCountNumber--;
               item.remove();
            }
            if (user_ID != 0){
                cleanCart("all");
            }
            
            Stage current_stage = (Stage) flowPane.getScene().getWindow();
            current_stage.close();
            
            infoAlert("Objednávka byla dokončena! Informace o obejdnávce jsou zapsané v databázi.");
        }

        allItems();      
    }
    
    /**
     * Inicializace vypiše všechny položky v košíku a pokud uživatel je přihlašený,
     * načte ze serveru jeho údaje.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allItems();
        
        if (user_ID != 0){
            try {
                getUserInfo();
            } catch (ClassNotFoundException ex) {
                errorAlert("Nelze se připojít k serveru. Opravte připojení.");
            }
        } else {
            first_text.setText("Před zaplacením budete muset zadat doručovací údaje!");
            second_text.setText("Pokud chcete uložit svoje údaje, zaregistrujte se.");
            user_name.setText(null);
            del_address.setText(null);
            del_city.setText(null);
            del_postcode.setText(null);
        }    
    }      
}
