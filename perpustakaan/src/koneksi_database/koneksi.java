/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package koneksi_database;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author ASUS
 */
public class koneksi {
    private static Connection mysqlkonek;
    public static Connection koneksiDB() throws SQLException {
    if(mysqlkonek==null){
               try {
                   String DB="jdbc:mysql://localhost:3306/db_perpus";
                   String user="root"; 
                   String pass=""; 
                   
                   DriverManager.registerDriver(new com.mysql.jdbc.Driver());
                   mysqlkonek = (Connection) DriverManager.getConnection(DB,user,pass);
                   System.out.println("Sukses");                   
               } catch (Exception e) {
                   JOptionPane.showMessageDialog(null,"gagal koneksi");
               }
    }
    return mysqlkonek;
    }
}
