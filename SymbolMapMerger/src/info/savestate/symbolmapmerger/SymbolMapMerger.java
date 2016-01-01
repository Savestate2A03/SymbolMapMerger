/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.savestate.symbolmapmerger;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Joseph El-Khouri 
 */
public class SymbolMapMerger {

    SymbolMap m1;
    SymbolMap m2;
    SymbolMap merged;
    
    public SymbolMapMerger(SymbolMap m1, SymbolMap m2) { 
        this.m1 = m1;
        this.m2 = m2;
        merged = null;
    }
    
    public void stripArtifacts() {
        System.out.println("Stripping artifact functions...");
        System.out.println("(Functions not shared by either map)");
        ArrayList<Symbol> new1 = new ArrayList<>();
        ArrayList<Symbol> new2 = new ArrayList<>();
        ArrayList<Symbol> old1 = new ArrayList(m1.getSymbols());
        ArrayList<Symbol> old2 = new ArrayList(m2.getSymbols());
        int count = 0;
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
            count++;
            if ((count % 471) == 0) {
                System.out.print("\rProgress... " + count + "/" + m1.getSymbols().size());
            }
        }
        System.out.print("Progress... " + count + "/" + m1.getSymbols().size());
        System.out.println();
        m1 = new SymbolMap(new1);
        m2 = new SymbolMap(new2);
        System.out.println("Artifact functions in map #1: " + old1.size());
        System.out.println("Artifact functions in map #2: " + old2.size());
    }
    
    public void merge() {
        System.out.println("Merging SymbolMaps...");
        ArrayList<Symbol> sm1 = new ArrayList(m1.getSymbols());
        ArrayList<Symbol> sm2 = new ArrayList(m2.getSymbols());
        ArrayList<Symbol> merged = new ArrayList<>();
        int conflicts = 0;
        for (int i=0; i<sm1.size(); i++) {
            Symbol s1 = sm1.get(i);
            Symbol s2 = sm2.get(i);
            if (!s1.name.equals(s2.name)) {
                if (!(s1.name.startsWith("zz") | s2.name.startsWith("zz"))) {
                    conflicts++;
                    merged.add(null);
                } else if (s1.name.startsWith("zz")) {
                    merged.add(s2);
                } else if (s2.name.startsWith("zz")) {
                    merged.add(s1);
                }
            } else {
                merged.add(s1);
            }
            if (i % 4 == 0)
               System.out.print("\rSize: " + i + "/" + sm1.size() +
                                " | Merged: " + merged.size() +
                                " | Conflicts: " + conflicts);
        }
        System.out.print("\rSize: " + sm1.size() + "/" + sm1.size() +
                         " | Merged: " + (merged.size()-conflicts) +
                         " | Conflicts: " + conflicts);
        System.out.println();
        this.merged = new SymbolMap(merged);
    }
    
    public void manualStrip() {
        Scanner reader = new Scanner(System.in);
        System.out.println("-- Conflict Resolution --");
        int conflicts = 0;
        for (Symbol s : merged.getSymbols()) {
            if (s == null)
                conflicts++;
        }
        boolean menu = true;
        char mode = '0';
        int offset = 0;
        while (menu) {
            System.out.println("Conflicts left: " + conflicts + "...");
            switch (mode) {
                case '0': 
                    System.out.println("[m]anual [a]uto [l]ist");
                    break;
                case 'l': 
                    ArrayList<Symbol>  conflicts1 = new ArrayList<>();
                    ArrayList<Symbol>  conflicts2 = new ArrayList<>();
                    for (int i=0; i<merged.getSymbols().size(); i++) {
                        Symbol s = merged.getSymbols().get(i);
                        if (s == null) {
                            conflicts1.add(m1.getSymbols().get(i));
                            conflicts2.add(m2.getSymbols().get(i));
                        }
                    }
                    for (int i=offset; i<offset+5 && i<conflicts1.size() && i>=0; i++) {
                        System.out.println(i + ": " + Integer.toUnsignedString(conflicts1.get(i).address, 16) + " --> " 
                                             + conflicts1.get(i).name + " | " + conflicts2.get(i).name);
                    }
                    System.out.println("[d]own [u]p | Choose [1] [2]");
                    break;
            }
            System.out.print(" --> ");
            mode = reader.next().charAt(0);
            switch (mode) {
                case 'd': 
                    offset += 5;
                    mode = 'l';
                    break;
                case 'u':
                    offset -= 5;
                    mode = 'l';
                    break;
            }            
            reader = new Scanner(System.in);
        }
    }
    
}
