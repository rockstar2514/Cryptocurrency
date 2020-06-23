package com.BlockChain.Node;

import java.util.ArrayList;

public class BlockChain {
     ArrayList<Block> ledger;
     //TODO define a way to get a BlockChain from another node data
     protected void addBlock(Block b) {
    	 ledger.add(b);
    	 //TODO here call a function to all transaction in the blockchan that will remove the respective inputs and insert in unused Outputs
     }
     //TODO much things left
     protected Block getBlock(int i) {
    	 return ledger.get(i);
     }
     protected int size() {
    	 return ledger.size();
     }
     protected boolean add(Block b) {
    	 ledger.add(b);
    	 return true;
     }
     
}

