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
        System.out.println("Prepend your own functions with a string?");
        System.out.println("(If you don't want to, press enter with nothing typed in)");
        System.out.print(" --> ");
        Scanner sc = new Scanner(System.in);
        String prepend = sc.nextLine();
        System.out.println("Merging SymbolMaps...");
        ArrayList<Symbol> sm1 = new ArrayList(m1.getSymbols());
        ArrayList<Symbol> sm2 = new ArrayList(m2.getSymbols());
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
            Symbol s1 = m1.getSymbols().get(i);
            Symbol s2 = m2.getSymbols().get(i);
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
            Symbol s1 = m1.getSymbols().get(i);
            Symbol s2 = m2.getSymbols().get(i);
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
            Symbol s1 = m1.getSymbols().get(i);
            Symbol s2 = m2.getSymbols().get(i);
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
            Symbol s1 = m1.getSymbols().get(i);
            Symbol s2 = m2.getSymbols().get(i);
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
            Symbol s1 = m1.getSymbols().get(i);
            Symbol s2 = m2.getSymbols().get(i);
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
