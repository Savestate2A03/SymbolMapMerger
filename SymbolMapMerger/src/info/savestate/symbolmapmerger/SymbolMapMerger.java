/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.savestate.symbolmapmerger;

import java.util.ArrayList;

/**
 *
 * @author Joseph El-Khouri 
 */
public class SymbolMapMerger {

    SymbolMap m1;
    SymbolMap m2;
    
    public SymbolMapMerger(SymbolMap m1, SymbolMap m2) { 
        this.m1 = m1;
        this.m2 = m2;
    }
    
    public void stripArtifacts() {
        ArrayList<Symbol> new1 = new ArrayList<>();
        ArrayList<Symbol> new2 = new ArrayList<>();
        ArrayList<Symbol> old1 = new ArrayList(m1.getSymbols());
        ArrayList<Symbol> old2 = new ArrayList(m2.getSymbols());
        for (Symbol s1 : m1.getSymbols()) {
            boolean found = false;
            int index1 = 0;
            int index2 = 0;
            for (Symbol s2 : m2.getSymbols()) {
                if (s1.address == s2.address) {
                    found = true;
                    index1 = old1.indexOf(s1);
                    index2 = old2.indexOf(s2);
                }
            }
            if (found) {
                new1.add(old1.remove(index1));
                new2.add(old2.remove(index2));
            }
        }
        SymbolMap processedSymbolMap1 = new SymbolMap(new1);
        SymbolMap processedSymbolMap2 = new SymbolMap(new2);
        System.out.println("Artifact functions in map #1: " + old1.size());
        System.out.println("Artifact functions in map #2: " + old2.size());
    }
    
}
