/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andrusyak.e.shop.server;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 * Serverová hlavní třida.
 * @author Andrusyak Viktor
 */
public class AndrusyakEShopServer {

    /** Deklarace socketu pro připojení klientů. */
    static Socket socket = null;
    
    /**
     * Třída, která funguje na principu Threadu.
     * Její instance se vytvoří, pokud se na socket připojí nový uživatel.
     * S pomocí Readeru čte přichozí zprávy a podle nich provádi akce pro
     * komunikaci uživatele s databázi.
     */
    static class ClientThread extends Thread {
        @Override
        public void run(){
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ){
                
                /**
                 * Deklarace potřebných údajů pro komunikaci s databázi.
                 */
                String url_db = "jdbc:mysql://160.217.213.13:33306/test";
                String user = "student";
                String password = "op18";
                Connection con;
                PreparedStatement pst;
                
 
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    
                    System.out.println("Přišel požadavek: " + inputLine);
                    
                    /** Pošle klientovi seznam všech produktů. */
                    if (inputLine.equals("listOfProducts")){
                                          
                        String sql = "SELECT * FROM av_product";
                
                        con = DriverManager.getConnection (url_db, user, password);
                        pst = con.prepareStatement(sql);
                        ResultSet result = pst.executeQuery(); 
                                       
                        int count = result.getMetaData().getColumnCount();
                                            
                        String [] resultList = new String[count];
                        
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        
                        while (result.next()){
                            for (int i = 1; i < count + 1; i++){
                                resultList[i-1] = result.getString(i); 
                            }

                            out.writeUnshared(resultList);
                        }                        
                        out.writeObject("quit");
                      
                        pst.close();
                        con.close();   
                        
                    } 
                    /**
                     * Příjme registrační údaje.
                     * Pokud zjistí, že se jedná o vytvoření účtu pro admina,
                     * vytvoří kód, který se bude muset na straně klienta zadat,
                     * až pak uživatel bude zaregistrovaný.
                     */
                    else if (inputLine.equals("registration")){
                        
                        ObjectInputStream newUserStream = new ObjectInputStream(socket.getInputStream()); 
                        
                        Object result = newUserStream.readObject();
                        
                        String[] resultList =(String[]) result;
                        
                        String sql = "INSERT INTO av_user(last_name, first_name, email, phone, street, house_number,"
                                + " city, postcode , permission, pass) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                        con = DriverManager.getConnection (url_db, user, password);
                        pst = con.prepareStatement(sql);
 
                        int permission = Integer.parseInt(resultList[8]);
                        
                        if (permission == 1){
                            Random rand = new Random();
                            int code = rand.nextInt(999999);
                            System.out.println("Ověřovácí kod pro nového admina: " + code +".");
                            
                            if ((inputLine = in.readLine()) != null){
                                if (inputLine.equals(code)){
                                    System.out.println("Registrace admina skončila NEúspěšně!");
                                    return;
                                }
                            }         
                        }
                        
                        pst.setString(1, resultList[0]);
                        pst.setString(2, resultList[1]);
                        pst.setString(3, resultList[2]);
                        pst.setString(4, resultList[3]);
                        pst.setString(5, resultList[4]);
                        pst.setString(6, resultList[5]);
                        pst.setString(7, resultList[6]);
                        pst.setString(8, resultList[7]);
                        pst.setInt(9, permission);
                        pst.setString(10, resultList[9]);
                        
                        pst.execute();
                        
                        pst.close();
                        con.close(); 
  
                    }                     
                    /**
                     * Zkontroluje v databázim jestli přichozí údaje existuji v databází.
                     * Pokud ano, zpátky pošle informaci o přihlášenému uživateli.
                     */
                    else if (inputLine.equals("sign_in")){
                        
                        String usermail, userpassword;
                        
                        usermail = in.readLine();
                        userpassword = in.readLine();
                        
                        String sql = "SELECT user_ID, permission, last_name, first_name FROM av_user WHERE email=? AND pass=?;";
                
                        con = DriverManager.getConnection (url_db, user, password);
                        pst = con.prepareStatement(sql);
                                       
                        pst.setString(1, usermail);
                        pst.setString(2, userpassword);
                                       
                        ResultSet result = pst.executeQuery(); 
      
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
                                              
                        if (result.next()){
                            String user_ID = result.getString("user_ID");
                            String permission = result.getString("permission");
                            String last_name = result.getString("last_name");
                            String first_name = result.getString("first_name");
                            out.println(user_ID);
                            out.println(permission);
                            out.println(last_name);
                            out.println(first_name);
                            
                            String sql2 = "SELECT * FROM av_card_item WHERE user_ID =" + user_ID; // načte položky z košíku
                            pst = con.prepareStatement(sql2);
                            ResultSet result2 = pst.executeQuery();
                            while (result2.next()) {
                                String product_ID = result2.getString("product_ID");
                                out.println(product_ID);                              
                            }
                            out.println("end");
                            
                        } else out.println("false");
                                      
                        con.close();
                        pst.close();
                    } 
                    /** Příjme údaje o novém produktu a zapiše do databáze */
                    else if (inputLine.equals("createProduct")){
                        ObjectInputStream newProduct = new ObjectInputStream(socket.getInputStream()); 
                        
                        Object result = newProduct.readObject();
                        
                        int length = newProduct.readInt();
                        byte[] image = new byte[length];
                        if(length>0) {
                            newProduct.read(image);
                        }
                           
                        String[] resultList =(String[]) result;
                
                        int price = Integer.parseInt(resultList[4]);
                        int amount = Integer.parseInt(resultList[5]);
                        
                        String sql = "INSERT INTO av_product(name, category, description_short, description_long, picture, price, amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                        con = DriverManager.getConnection (url_db, user, password);
                        pst = con.prepareStatement(sql);
                        
                        pst.setString(1, resultList[0]);
                        pst.setString(2, resultList[1]);
                        pst.setString(3, resultList[2]);
                        pst.setString(4, resultList[3]);
                        pst.setBinaryStream(5,new ByteArrayInputStream(image), length);
                        pst.setInt(6, price);
                        pst.setInt(7, amount);
                        
                        pst.execute();
                        
                        pst.close();
                        con.close();  
                    } 
                    
                    /** Zapiše do databáze novou položku v košíku. */
                    else if (inputLine.equals("toCard")){
                                           
                        String product_ID = in.readLine();
                        int user_ID = in.read();
                        int product_ID_int = Integer.parseInt(product_ID);
                        
                        
                        String sql = "INSERT INTO av_card_item(user_ID, product_ID) VALUES (?, ?)";
                
                        con = DriverManager.getConnection (url_db, user, password);
                        pst = con.prepareStatement(sql);
    
                        pst.setInt(1, user_ID);
                        pst.setInt(2, product_ID_int);
 
                        pst.execute();
                        
                        pst.close();
                        con.close(); 
  
                    }
                    
                    /** Odešle uživateli kompletně vechny údaje o něm. */
                    else if (inputLine.equals("getUserInfo")){
                        
                        String user_ID = in.readLine();
                        
                        String sql = "SELECT * FROM av_user WHERE user_ID=" + user_ID;
                
                        con = DriverManager.getConnection (url_db, user, password);
                        pst = con.prepareStatement(sql);
                        ResultSet result = pst.executeQuery(); 
                        
                        if (result.next()){                            
                            int count = result.getMetaData().getColumnCount();

                            String [] resultList = new String[count];

                            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());


                            for (int i = 1; i < count + 1; i++){
                                resultList[i-1] = result.getString(i);
                            }

                            out.writeUnshared(resultList);
                        }
                           
                        pst.close();
                        con.close();       
                    }   
                        
                    /** Vytvoří novou objednávku podle přichozích údajů */
                    else if (inputLine.equals("createOrder")){
                        
                        ObjectInputStream newOrder = new ObjectInputStream(socket.getInputStream()); 
                        
                        Object result = newOrder.readObject();
                        
                        String[] resultList =(String[]) result;
                        
                        String sql = "INSERT INTO av_order(date, del_last_name, del_first_name, del_email, del_phone, del_street, del_house_num,"
                                + " del_city, del_postcode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                        con = DriverManager.getConnection (url_db, user, password);
                        pst = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
 
                        
                        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
                        pst.setDate(1, new java.sql.Date(date.getTime()));
                        pst.setString(2, resultList[1]);
                        pst.setString(3, resultList[2]);
                        pst.setString(4, resultList[3]);
                        pst.setString(5, resultList[4]);
                        pst.setString(6, resultList[5]);
                        pst.setString(7, resultList[6]);
                        pst.setString(8, resultList[7]);
                        pst.setString(9, resultList[8]);

                        
                        pst.executeUpdate();
                        
                        ResultSet keys = pst.getGeneratedKeys(); 
                        int order_ID;
                        keys.next();
                        order_ID = keys.getInt(1);
                        
                        String sql2 = "INSERT INTO av_item(order_ID, product_ID, amount, price_per_piece) VALUES (?, ?, ?, ?)";
                        String sql3 = "UPDATE av_product SET amount = ? WHERE product_ID = ?";
    
                        while (true){  
                            
                            try {
                            Object newItem = newOrder.readObject();

                            String[] item =(String[]) newItem;
                            
                            int count = newOrder.readInt();
                            
                            pst = con.prepareStatement(sql2);                 
                            pst.setInt(1, order_ID);
                            pst.setString(2, item[0]);
                            pst.setInt(3, count);
                            pst.setString(4, item[6]);
                            
                            pst.execute(); 
                            
                            count = Integer.parseInt(item[7]) - count; 
                            pst = con.prepareStatement(sql3);
                            pst.setInt(1, count);
                            pst.setString(2, item[0]);
                            
                            pst.execute();
                            
                            } catch (IOException e) {
                                pst.close();
                                con.close();
                                break;
                            }  
                        } 
                    } 
                    
                    /** Změní množstvi konkretního produktu */
                    else if (inputLine.equals("addCount")){

                       String product_ID = in.readLine();
                       int amount = in.read();

                       String sql = "UPDATE av_product SET amount = ? WHERE product_ID = ?";

                       con = DriverManager.getConnection (url_db, user, password);
                       pst = con.prepareStatement(sql);

                       pst.setInt(1, amount);
                       pst.setString(2, product_ID);


                       pst.execute();

                       pst.close();
                       con.close();           
                    }
                    
                    /** Vymaže jednu nebo všechny položky v košíku u uživatele. */
                    else if (inputLine.equals("cleanCart")){
                         
                        con = DriverManager.getConnection (url_db, user, password);
                        
                        String user_ID = in.readLine();
                        
                        String description = in.readLine();
                               
                        
                        String sql;
                        if (description.equals("all")){
                            sql = "DELETE FROM av_card_item WHERE user_ID = ?";
                            pst = con.prepareStatement(sql);
                            pst.setString(1, user_ID);       
                        } else {
                            sql = "DELETE FROM av_card_item WHERE user_ID = ? AND product_ID = ?";
                            pst = con.prepareStatement(sql);
                            pst.setString(1, user_ID);
                            pst.setString(2, description);
                        }
                        
                        pst.execute();
                        
                        pst.close();
                        con.close();           
                     }
                         
                }      
            } catch (IOException ex) {
                System.out.println("Došlo k chybě připojení! " + ex);
            } catch (SQLException ex) {
                System.out.println("Došlo k chybě v databází! " + ex);
            } catch (ClassNotFoundException ex) {
                System.out.println("Došlo k chybě při registraci! " + ex);
            }
        }  
    }
    
    
    public static void main(String[] args) throws IOException {  
        /** Port, na kterém běří server. */
        int portNumber = 8888;
        /** deklarace serverového socketu. */
        ServerSocket server;
        
        /** Inicializace serverového socketu. */
        server = new ServerSocket(portNumber);
        
        /**
         * Přijíma nová připojení na server a vytváří instance ClientThreadů.
         */
        while (true){
            socket = server.accept();
            new ClientThread().start();
        }    
        
    }
    
}
