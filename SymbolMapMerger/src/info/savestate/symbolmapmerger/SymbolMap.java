/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.savestate.symbolmapmerger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * 
 * @author Joseph
 */
public class SymbolMap {
    
    private ArrayList<Symbol> symbols;
    
    public static SymbolMap symbolMapBuilder(String filepath) {
        SymbolMap sm = new SymbolMap();
        sm.symbols = new ArrayList<>();
        try {
            InputStream fis = new FileInputStream(filepath);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals(".text"))
                        continue;
                sm.symbols.add(Symbol.symbolBuilder(line));
            }
            return sm;
        } catch (IOException ex) {
            return null;
        }
    }
    
    public void printSymbolMap() {
        for (Symbol s : symbols) {
            System.out.println(s);
        }
    }
    
}
