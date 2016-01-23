/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.savestate.symbolmapmerger;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Joseph El-Khouri 
 */
public class SymbolMapMerger {

    SymbolMap csm;
    SymbolMap yourMap;
    SymbolMap merged;
    
    public SymbolMapMerger(SymbolMap csm, SymbolMap yourMap) { 
        this.csm = csm;
        this.yourMap = yourMap;
        merged = null;
    }
    
    public void stripArtifacts() {
        System.out.println("Stripping artifact functions...");
        System.out.println("(Functions not shared by either map)");
        System.out.println("... except for NAMED functions present in the CSM");
        ArrayList<Symbol> newCSM = new ArrayList<>();
        ArrayList<Symbol> newYourMap = new ArrayList<>();
        ArrayList<Symbol> oldCSM = new ArrayList(csm.getSymbols());
        ArrayList<Symbol> oldYourMap = new ArrayList(yourMap.getSymbols());
        int count = 0;
        for (Symbol csmSymbol : csm.getSymbols()) {
            boolean found = false;
            boolean csmOnlySymbol = true;
            int indexCSM = oldCSM.indexOf(csmSymbol);
            int indexYourMap = 0;
            for (Symbol yourSymbol : yourMap.getSymbols()) {
                if (csmSymbol.address == yourSymbol.address) {
                    found = true;
                    csmOnlySymbol = false;
                    indexYourMap = oldYourMap.indexOf(yourSymbol);
                    break;
                }
            }
            if (csmSymbol.name.startsWith("zz") && csmOnlySymbol)
                csmOnlySymbol = false;
            if (found) {
                newCSM.add(oldCSM.remove(indexCSM));
                newYourMap.add(oldYourMap.remove(indexYourMap));
            } else if (csmOnlySymbol) {
                newYourMap.add(oldCSM.get(indexCSM).duplicate());
                newCSM.add(oldCSM.remove(indexCSM));
            }
            count++;
            if ((count % 471) == 0) {
                System.out.print("\rProgress... " + count + "/" + csm.getSymbols().size());
            }
        }
        System.out.print("Progress... " + count + "/" + csm.getSymbols().size());
        System.out.println();
        csm = new SymbolMap(newCSM);
        yourMap = new SymbolMap(newYourMap);
        System.out.println("Artifact functions in map #1: " + oldCSM.size());
        System.out.println("Artifact functions in map #2: " + oldYourMap.size());
    }
    
    public void merge() {
        System.out.println("Prepend your own functions with a string?");
        System.out.println("(If you don't want to, press enter with nothing typed in)");
        System.out.print(" --> ");
        Scanner sc = new Scanner(System.in);
        String prepend = sc.nextLine();
        System.out.println("Merging SymbolMaps...");
        ArrayList<Symbol> sm1 = new ArrayList(csm.getSymbols());
        ArrayList<Symbol> sm2 = new ArrayList(yourMap.getSymbols());
        ArrayList<Symbol> mergedSymbolMap = new ArrayList<>();
        int conflicts = 0;
        for (int i=0; i<sm1.size(); i++) {
            Symbol s1 = sm1.get(i);
            Symbol s2 = sm2.get(i);
            if (!s1.name.equals(s2.name)) {
                if (!(s1.name.startsWith("zz") | s2.name.startsWith("zz"))) {
                    conflicts++;
                    mergedSymbolMap.add(null);
                } else if (s1.name.startsWith("zz")) {
                    if (!prepend.isEmpty()) {
                        if (!s2.name.startsWith(prepend))
                            s2.name = prepend + s2.name;
                    }
                    mergedSymbolMap.add(s2);
                } else if (s2.name.startsWith("zz")) {
                    mergedSymbolMap.add(s1);
                }
            } else {
                mergedSymbolMap.add(s1);
            }
            if (i % 4 == 0)
               System.out.print("\rSize: " + i + "/" + sm1.size() +
                                " | Merged: " + mergedSymbolMap.size() +
                                " | Conflicts: " + conflicts);
        }
        System.out.print("\rSize: " + sm1.size() + "/" + sm1.size() +
                         " | Merged: " + (mergedSymbolMap.size()-conflicts) +
                         " | Conflicts: " + conflicts);
        System.out.println();
        // conflicting symbols are null
        merged = new SymbolMap(mergedSymbolMap);
    }
    
    public void conflictResolution() {
        System.out.println("Conflict resolution...");
        Scanner sc = new Scanner(System.in);
        while (merged.getSymbols().contains(null)) {
            System.out.println("Conflicts left: " + getNumConflicts());
            System.out.println("[m]anual\n[r]egex\n[c]ontains\n[s]tarts_with\n[l]ist");
            System.out.print(" --> ");
            String scanner = sc.nextLine();
            char menu = scanner.toLowerCase().charAt(0);
            switch (menu) {
                case 'm':
                    conflictManual();
                    break;
                case 'r':
                    conflictRegex();
                    break;
                case 'c':
                    conflictContains();
                    break;
                case 's':
                    conflictStartsWith();
                    break;
                case 'l':
                    conflictList();
                    break;
            }
        }
        System.out.println("Conflict resolution finished!");
    }
    
    private void conflictManual() {
        System.out.println("Manual conflict resolutions...");
        for (int i=0; i<merged.getSymbols().size(); i++) {
            Symbol s = merged.getSymbols().get(i);
            if (s != null) continue;
            System.out.println("Conflicts left: " + getNumConflicts());
            Scanner sc = new Scanner(System.in);
            Symbol s1 = csm.getSymbols().get(i);
            Symbol s2 = yourMap.getSymbols().get(i);
            String hex = Integer.toUnsignedString(s1.address, 16);
            System.out.println("0x" + hex + ": ");
            System.out.println("  [1] \"" + s1.name + "\"");
            System.out.println("  [2] \"" + s2.name + "\"");
            System.out.println("  [n]ext");
            System.out.println("  [p]rev");
            System.out.println("  [b]ack");
            System.out.print(" --> ");
            String string = sc.nextLine();
            if (string.isEmpty()) {
                i--;
                continue;
            }
            char option = string.toLowerCase().charAt(0);
            switch(option) {
                case '1':
                    merged.getSymbols().set(i, s1);
                    break;
                case '2':
                    merged.getSymbols().set(i, s2);                    
                    break;
                case 'n':
                    break;
                case 'p':
                    for (int j=i-1; j>=0; j--) {
                        Symbol temp = merged.getSymbols().get(j);
                        if (temp != null) continue;
                        i = j;
                        break;
                    }
                    i--;
                    break;
                case 'b':
                    return;
            }
        }
    }
    
    private void conflictRegex() {
        System.out.println("Regex conflict resolution...");
        System.out.println("  All symbol names that match with");
        System.out.println("  the following regex string will be");
        System.out.println("  saved. If both symbol names match");
        System.out.println("  with the provided regex, neither");
        System.out.println("  will be saved and will need to be");
        System.out.println("  resolved otherwise");
        System.out.print(" --> ");
        Scanner sc = new Scanner(System.in);
        String string = "";
        while(string.isEmpty()) string = sc.nextLine();
        int matches = 0;
        for (int i=0; i<merged.getSymbols().size(); i++) {
            Symbol s = merged.getSymbols().get(i);
            if (s != null) continue;
            Symbol s1 = csm.getSymbols().get(i);
            Symbol s2 = yourMap.getSymbols().get(i);
            try {
                if (s1.name.matches(string) && !s2.name.matches(string)) {
                    merged.getSymbols().set(i, s1); 
                    matches++;
                    System.out.println(" [" + s1.name +"] replaced [" + s2.name + "]");
                    continue;
                }
                if (s2.name.matches(string) && !s1.name.matches(string)) {
                    merged.getSymbols().set(i, s2);
                    matches++;
                    System.out.println(" [" + s2.name +"] replaced [" + s1.name + "]");
                }
            } catch (Exception e) {}
        }
        System.out.println(matches + " matches were made!");

    }
    
    private void conflictContains() {
        System.out.println("Contains conflict resolution...");
        System.out.println("  All symbol names that contain");
        System.out.println("  the following user input will be");
        System.out.println("  saved. If both symbol names contain");
        System.out.println("  the provided string, neither");
        System.out.println("  will be saved and will need to be");
        System.out.println("  resolved otherwise");
        System.out.print(" --> ");
        Scanner sc = new Scanner(System.in);
        String string = "";
        while(string.isEmpty()) string = sc.nextLine();
        int matches = 0;
        for (int i=0; i<merged.getSymbols().size(); i++) {
            Symbol s = merged.getSymbols().get(i);
            if (s != null) continue;
            Symbol s1 = csm.getSymbols().get(i);
            Symbol s2 = yourMap.getSymbols().get(i);
            if (s1.name.contains(string) && !s2.name.contains(string)) {
                merged.getSymbols().set(i, s1); 
                matches++;
                System.out.println(" [" + s1.name +"] replaced [" + s2.name + "]");
                continue;
            }
            if (s2.name.contains(string) && !s1.name.contains(string)) {
                merged.getSymbols().set(i, s2);
                matches++;
                System.out.println(" [" + s2.name +"] replaced [" + s1.name + "]");
            }
        }
        System.out.println(matches + " matches were made!");
    }
    
    private void conflictStartsWith() {
        System.out.println("Starts with conflict resolution...");
        System.out.println("  All symbol names that start with");
        System.out.println("  the following user input will be");
        System.out.println("  saved. If both symbol names start");
        System.out.println("  with the provided string, neither");
        System.out.println("  will be saved and will need to be");
        System.out.println("  resolved otherwise");
        System.out.print(" --> ");
        Scanner sc = new Scanner(System.in);
        String string = "";
        while(string.isEmpty()) string = sc.nextLine();
        int matches = 0;
        for (int i=0; i<merged.getSymbols().size(); i++) {
            Symbol s = merged.getSymbols().get(i);
            if (s != null) continue;
            Symbol s1 = csm.getSymbols().get(i);
            Symbol s2 = yourMap.getSymbols().get(i);
            if (s1.name.startsWith(string) && !s2.name.startsWith(string)) {
                merged.getSymbols().set(i, s1); 
                matches++;
                System.out.println(" [" + s1.name +"] replaced [" + s2.name + "]");
                continue;
            }
            if (s2.name.startsWith(string) && !s1.name.startsWith(string)) {
                merged.getSymbols().set(i, s2);
                matches++;
                System.out.println(" [" + s2.name +"] replaced [" + s1.name + "]");
            }
        }
        System.out.println(matches + " matches were made!");
    }
    
    private void conflictList() {
        System.out.println("Listing conflict resolutions...");
        for (int i=0; i<merged.getSymbols().size(); i++) {
            Symbol s = merged.getSymbols().get(i);
            if (s != null) continue;
            Symbol s1 = csm.getSymbols().get(i);
            Symbol s2 = yourMap.getSymbols().get(i);
            String hex = Integer.toUnsignedString(s1.address, 16);
            System.out.print("0x" + hex + ": [");
            System.out.print(s1.name);
            System.out.print("] [");
            System.out.print(s2.name);
            System.out.print("]\n");
        }
    }
    
    private int getNumConflicts() {
        int size = 0;
        for (Symbol s : merged.getSymbols()) {
            if (s == null) size++;
        }
        return size;
    }

    public void write(String name) {
        StringBuilder output = new StringBuilder();
        output.append(".text\n");
        for (int i=0; i<merged.getSymbols().size(); i++) {
            Symbol s = merged.getSymbols().get(i);
            output.append(Integer.toUnsignedString(s.address, 16));
            output.append(' ');
            output.append(s.leadingZeros(Integer.toUnsignedString(s.size, 16)));
            output.append(' ');
            output.append(Integer.toUnsignedString(s.address, 16));
            output.append(" 0 ").append(s.name);
            output.append("\n");
        }
        byte[] bytes = output.toString().getBytes(StandardCharsets.UTF_8);
        try {
            try (FileOutputStream stream = new FileOutputStream(name)) {
                stream.write(bytes);
            }
        } catch (Exception e) {
            System.out.println("Error writing symbol map!");
        }
    }
    
}
