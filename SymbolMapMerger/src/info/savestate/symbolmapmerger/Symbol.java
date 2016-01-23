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
public class Symbol implements Comparable<Symbol> {
    
    public int address;
    public int size;
    public String name;
    
    public Symbol(int address, int size, String name) {
        this.address = address;
        this.size = size;
        this.name = name;
    }

    public static Symbol symbolBuilder(String line) {
        String[] parts = line.split(" ", 5);
        int address = Integer.parseUnsignedInt(parts[0], 16);
        int size = Integer.parseInt(parts[1], 16);
        String fullname = parts[4];
        Symbol s = new Symbol(address, size, fullname);
        return s;
    }
      
    public String leadingZeros(String s) {
        String zeros = "";
        int amt = 8 - s.length();
        for (int i=0; i<amt; i++) 
            zeros += "0"; 
        return zeros + s;
    }
    
    public Symbol duplicate() {
        return new Symbol(address, size, name);
    }
    
    @Override
    public String toString() {
        String hex_ADDR = leadingZeros(Integer.toHexString(address));
        String hex_SIZE = leadingZeros(Integer.toHexString(size));
        StringBuilder sb = new StringBuilder();
        sb.append(hex_ADDR).append(" ");
        sb.append(hex_SIZE).append(" ");
        sb.append(hex_ADDR).append(" 0 ");
        sb.append(name);
        return sb.toString();
    }

    @Override
    public int compareTo(Symbol o) {
        return (Integer.compareUnsigned(this.address, o.address));
    }
    
}
