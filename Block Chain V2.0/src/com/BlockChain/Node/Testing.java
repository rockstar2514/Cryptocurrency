package com.BlockChain.Node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.Util;

public class Testing {
     public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidInputException, InvalidOutputException {
    	 InputStream reader=new FileInputStream("010.dat");
    	 byte[] tdata=reader.readAllBytes();
    	 Transaction t=Transaction.getTransaction(tdata);
    	 byte b1[]=t.getData();     
    	 reader=new FileInputStream("79d728fa8bf712b517da66bbe7c34278ea74a87cd385d3189ff3b436c7ae91bc.dat");
    	 tdata=reader.readAllBytes();
    	 t=Transaction.getTransaction(tdata);
    	 byte b2[]=t.getData();
    	 byte[] body=new byte[12+b1.length+b2.length];
    	 byte count[]=Util.toByte(2);
    	 for(int i=0;i<4;i++) {
    		 body[i]=count[i];
    	 }
    	 count=Util.toByte(b1.length);
    	 for(int i=0;i<4;i++) {
    		 body[4+i]=count[i];
    	 }
    	 for(int i=0;i<b1.length;i++)
    		 body[8+i]=b1[i];
    	 count=Util.toByte(b2.length);
    	 for(int i=0;i<4;i++) {
    		 body[8+b1.length+i]=count[i];
    	 }
    	 for(int i=0;i<b2.length;i++)
    		 body[12+b1.length+i]=b2[i];
    	 byte[] fulldata=new byte[116+body.length];
    	 for(int i=116;i<fulldata.length;i++) {
    		 fulldata[i]=body[i-116];
    	 }
    	 Block b=new Block(fulldata);
    	 b.getReceipt();
     }
}
