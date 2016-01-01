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
        System.out.println(" ================================================");
        System.out.println(" | Dolphin Symbol Merger                        |");
        System.out.println(" | Made by Savestate for Absolome's CSM Project |");
        System.out.println(" | http://smashboards.com/threads/smashboards-  |"
                         + "\n"
                         + " | community-symbol-map.426763/                 |");
        System.out.println(" ================================================");
        SymbolMap sm1 = SymbolMap.symbolMapBuilder(args[0]);
        SymbolMap sm2 = SymbolMap.symbolMapBuilder(args[1]);
        SymbolMapMerger smm = new SymbolMapMerger(sm1, sm2);
        smm.stripArtifacts();
        smm.merge();
        smm.manualStrip();
    } 
    
}
