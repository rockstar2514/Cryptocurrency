package com.BlockChain.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.bouncycastle.util.Arrays;

public class BlockChain {
     private Block last;
     private String dir;
     private String target;
     private int size=0;
     //TODO define a way to get a BlockChain from another node data
     protected BlockChain(String d,String target) {
    	 dir=d;
    	 this.target=target;
     }
     protected void addBlock(Block b) {	 
    try {Files.write(Paths.get(dir), Arrays.concatenate(b.getHeader(),b.getData()), StandardOpenOption.APPEND);size++;last=b;} catch (IOException e) {System.out.println("File to print Block Chain to not found . Fuck OFf");System.exit(1);e.printStackTrace();}	 
     }
     //TODO much things left
     protected int getSize() {
    	 return size;
     }
     protected Block getTop() {
    	 return last;
     }
     protected String getTarget() {
    	 return target;
     }
}

