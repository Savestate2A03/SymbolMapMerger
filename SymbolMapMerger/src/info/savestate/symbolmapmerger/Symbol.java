/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.savestate.symbolmapmerger;

/**
 *
 * @author Joseph
 */
public class Symbol {
    
    public int address;
    public int size;
    public String name;
    public String author;
    
    public Symbol(int address, int size, String fullname) {
        this.address = address;
        this.size = size;
        String[] name_author = Symbol.nameSplit(fullname);
        if (name_author.length == 1) {
            name = name_author[0];
            author = "";
        } else {
            author = name_author[0];
            name = name_author[1];
        }
    }
    
    public Symbol(int address, int size, String name, String author) {
        this.address = address;
        this.size = size;
        this.name = name;
        this.author = author;
    }
    
    public static String[] nameSplit(String fullname) {
        return fullname.split("_", 2);
    }
    
    public static String nameJoin(String name, String author) {
        if (author.isEmpty())
            return name;
        return author + "_" + name;
    }
    
    public static Symbol symbolBuilder(String line) {
        String[] parts = line.split(" ", 5);
        int address = Integer.parseInt(parts[0], 16);
        int size = Integer.parseInt(parts[1], 16);
        String fullname = parts[4];
        Symbol s = new Symbol(address, size, fullname);
        return s;
    }
      
    private String leadingZeros(String s) {
        String zeros = "";
        int amt = 8 - s.length();
        for (int i=0; i<amt; i++) 
            zeros += "0"; 
        return zeros + s;
    }
    
    @Override
    public String toString() {
        String hex_ADDR = leadingZeros(Integer.toHexString(address));
        String hex_SIZE = leadingZeros(Integer.toHexString(size));
        String function = Symbol.nameJoin(name, author);
        StringBuilder sb = new StringBuilder();
        sb.append(hex_ADDR).append(" ");
        sb.append(hex_SIZE).append(" ");
        sb.append(hex_ADDR).append(" 0 ");
        sb.append(function);
        return sb.toString();
    }
    
}
