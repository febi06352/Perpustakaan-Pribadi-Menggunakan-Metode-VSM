/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package perpustakaan;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ASUS
 */
public class freqDokumen {
    
    String namaDokumen;
    Map<String, Integer> pemetaan = new HashMap<>();
    public freqDokumen(String namaDokumen){
        this.namaDokumen = namaDokumen;
    }
    
    public void put(String kata){
        Integer frekuensi = pemetaan.get(kata);
        pemetaan.put(kata,(frekuensi==null) ? 1 : frekuensi+1);
    }
    
    public Integer getBanyak(String kata){
        return pemetaan.get(kata);
    }
    
    public String getNamaDokumen(){
        return this.namaDokumen;
    }
}
