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
        author = name_author[0];
        name = name_author[1];
    }
    
    public Symbol(int address, int size, String name, String author) {
        
    }
    
    public static String[] nameSplit(String fullname) {
        return fullname.split("_", 2);
    }
    
    public static String nameJoin(String name, String author) {
        return author + "_" + name;
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
