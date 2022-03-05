/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andrusyak_shop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

/**
 * FXML Controller, zodpovědný za okno přihlášení nebo registrace.
 *
 * @author Andrusyak Viktor
 */
public class AuthenticationController extends MainFXMLController implements Initializable {

    /**
     * Deklarace všech Inputů, potřebných pro přihlášení nebo registraci.
     */
    @FXML
    private TextField reg_last_name, reg_name, reg_email, reg_mobile,
            reg_street, reg_house, reg_city, reg_postcode, sign_email;
    @FXML
    private PasswordField reg_password, sign_password;
    

    /**
     * Metoda se provede po stisknutí tlačitka na registraci.
     * Zkontroluje některé vstupní pole a spojí se se serverm pro odeslaní údajů.
     * @param event 
     */
    @FXML
    void registration(ActionEvent event) {
        String permission = "0"; // určuje jestli se jedná o registraci admina nebo obyčejného uživatele.
        
        
        // kontrola mailu        
        if (!reg_email.getText().matches("^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$")) {        
            errorAlert("Neplatný mail.");
            return;
        } else if (reg_email.getText().contains("@admin.shop")){
            permission = "1";
        } 
        // kontrola telefonu
        if (reg_mobile.getText().matches("\\d{8}|\\d{11}")) {
            errorAlert("Neplatné telefonní číslo.");
            return;
        }
        
        if (reg_last_name.getText().trim().isEmpty() || reg_name.getText().trim().isEmpty() ||
                reg_street.getText().trim().isEmpty() || reg_house.getText().trim().isEmpty() ||
                reg_city.getText().trim().isEmpty() || reg_postcode.getText().trim().isEmpty()
                || reg_password.getText().trim().isEmpty()){
            errorAlert("Některé údaje nejsou zadány.");
            return;
        }
            
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){    

            out.println("registration");
                
            String [] newUser = {
                reg_last_name.getText(), reg_name.getText(), reg_email.getText(),
                reg_mobile.getText(), reg_street.getText(), reg_house.getText(),
                reg_city.getText(), reg_postcode.getText(), permission,
                reg_password.getText()
            };
            
            ObjectOutputStream newUserStream = new ObjectOutputStream(socket.getOutputStream());
           
            newUserStream.writeObject(newUser);
            
            if (permission.equals("1")){
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Ověřování");
                dialog.setHeaderText(null);
                dialog.setContentText("Ověřovácí kod:");
                Optional<String> result = dialog.showAndWait();
                
                out.println(result);             
            }
            

            infoAlert("Registrace byla úspěšně dokončena! Přihlaste se.");
 
            Stage current_stage = (Stage) reg_password.getScene().getWindow();
            current_stage.close();
            

        } catch (IOException ex) {
            errorAlert("Buď máte nefunkční připojení nebo přestal fungovat server!");
            
            Stage current_stage = (Stage) reg_password.getScene().getWindow();
            current_stage.close();
        }   
    }
    
    /**
     * Metoda se provede po stisknutí tlačitka na přihlášení.
     * Získa ze serveru odpověď jestli uživatel existuje a pokud ano, načte informace o něm.
     * @param event 
     */
    @FXML
    void sign_in(ActionEvent event) {       
        try (  
            Socket socket = new Socket(inetAddress, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);         
        ){   
            out.println("sign_in");
            
            out.println(sign_email.getText());
            out.println(sign_password.getText());
              
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                   
            String result = in.readLine();
                
            if (result.equals("false")){
                errorAlert("Přihlašovácí údaje nejsou správné!");

                Stage current_stage = (Stage) reg_password.getScene().getWindow();
                current_stage.close();
            } else {

                user_ID = Integer.parseInt(result);
                permission = Integer.parseInt(in.readLine());
                
                user_last_name = in.readLine();
                user_first_name = in.readLine();
                
                
                
                System.out.println("Byl přihlášen uživatel s ID " + user_ID + " a oprávnění " + permission);
                isLogged = true;
                
                
                myCard.clear();
                cardCountNumber = 0;
                String cardItem;
                while (!(cardItem = in.readLine()).equals("end")){                 
                    for (String[] item : allProducts){
                        if (cardItem.equals(item[0])) {
                            myCard.add(item);                            
                            cardCountNumber++;
                        }
                    }
                }
                
                infoAlert("Dobrý den, " + user_last_name + " " + user_first_name + "!");
                
                Stage current_stage = (Stage) reg_password.getScene().getWindow();  
                current_stage.close();      
            }
            
        } catch (IOException ex) {
            
            errorAlert("Buď máte nefunkční připojení nebo přestal fungovat server!");
            
            Stage current_stage = (Stage) reg_password.getScene().getWindow();
            current_stage.close();
        } 
    }
    
    

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Žádna inicializace se neprovádí.
    
    }    
    
}
