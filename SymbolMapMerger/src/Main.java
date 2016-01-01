/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import info.savestate.symbolmapmerger.*;

/**
 *
 * @author Joseph El-Khouri
 */
public class Main {
    
    public static void main(String[] args) {
        SymbolMap sm1 = SymbolMap.symbolMapBuilder(args[0]);
        SymbolMap sm2 = SymbolMap.symbolMapBuilder(args[1]);
        SymbolMapMerger smm = new SymbolMapMerger(sm1, sm2);
        smm.stripArtifacts();
    } 
    
}
