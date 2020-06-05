package com.BlockChain.Node;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.Util;

class Input {
     private int inputs;
     private String ids[];
     private int outputIndex[];
     private String signatures[];
     private byte[] data;
     protected Input(int inputs,String ids[],int outputIndex[],String signatures[]) throws InvalidInputException{
    	   this.inputs=inputs;
    	   this.ids=new String[ids.length];
    	   for(int i=0;i<ids.length;i++) {
    		   this.ids[i]=ids[i];
    	   }
    	   this.outputIndex=new int[outputIndex.length];
    	   for(int i=0;i<outputIndex.length;i++) {
    		   this.outputIndex[i]=outputIndex[i];
    	   }
    	   this.signatures=new String[signatures.length];
    	   for(int i=0;i<signatures.length;i++) {
    		   this.signatures[i]=signatures[i];
    	   }  
    	   if(!this.verify())
    		   throw new InvalidInputException();
           ArrayList<Byte> dat=new ArrayList<Byte>();
		   byte[] inps=Util.toByte(inputs);
		   Util.append(dat,inps);
		   for(int i=0;i<inputs;i++){
			   String hexa=ids[i];
			   byte[] transid=Util.parseHexToByte(hexa);//verify once
			   Util.append(dat,transid);
			   byte[] ind=(Util.toByte(outputIndex[i]));
			   Util.append(dat,ind);
			   String sign=signatures[i];
			   byte[] signature=Util.parseHexToByte(sign);
			   int len=signature.length;
			   byte[] signlen=(Util.toByte(len));
			   Util.append(dat,signlen);
			   Util.append(dat,signature);
		  }
		  data=new byte[dat.size()];
		  for(int i=0;i<data.length;i++) {
			  data[i]=dat.get(i);
		  }
     }
     protected byte[] getData() {
    	 byte[] copy=new byte[data.length];
    	 for(int i=0;i<copy.length;i++) {
    		 copy[i]=data[i];
    	 }
    	 return copy;
     }
     private boolean verify() {
    	 //TODO apply checks
    	 return true;
     }
     protected static Input read(File f,int offset) throws IOException, InvalidInputException {
    	 FileInputStream fis = new FileInputStream(f);
 	    BufferedInputStream reader = new BufferedInputStream(fis);
 	    if(offset!=0)
 	    {	
 	    	reader.read(new byte[offset],0,offset);
 	    }
 	    byte[] inps=new byte[4];
 	    reader.read(inps,0,4);
 	    int inputs=Util.toInt(inps);
 	    String ID[]=new String[inputs];
 	    int outputIndex[]=new int[inputs];
 	    String signatures[]=new String[inputs];
 	    for(int i=0;i<inputs;i++){
 	    	byte[] transid=new byte[32];
 	    	reader.read(transid,0,32);
 	    	ID[i]=Util.parseByteToHex(transid);
 	    	byte[] dum=new byte[4];
 	    	reader.read(dum,0,4);
 	    	outputIndex[i]=Util.toInt(dum);
 	    	reader.read(dum,0,4);
 	    	int length=Util.toInt(dum);
 	    	byte[] sign=new byte[length];
 	    	reader.read(sign,0,length);
 	    	signatures[i]=Util.parseByteToHex(sign);
 	    }
 	   reader.close();
 	    return new Input(inputs,ID,outputIndex,signatures);
 	   
     }
     protected void print() {
    	 System.out.println("Number of Inputs "+inputs+"\n\n");
  	   
 	    for(int i=0;i<inputs;i++)
 	    {
 	    	  System.out.println("      Input #"+(i+1));
 	    	  System.out.print("               ");
 	          System.out.println("Transaction ID : "+ ids[i]);
 	    	  System.out.print("               ");
 	          System.out.println("Output Index: "+outputIndex[i]);
 	          System.out.print("               ");
 	          System.out.println("Length of Signature: "+signatures[i].length()/2);//vefiy once again
 	          System.out.print("               ");
 	          System.out.println("Signature: "+signatures[i]+"\n\n");
 	    }
     }
     
}
