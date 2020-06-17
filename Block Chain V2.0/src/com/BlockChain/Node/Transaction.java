package com.BlockChain.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import com.Util;

//TODO take are of Object handling in memory there is a risk
public class Transaction {
       protected Input getInput() {
		return input;
	}
	private void setInput(Input input) {
		this.input = input;
	}
	protected Output getOutput() {
		return output;
	}
	private void setOutput(Output output) {
		this.output = output;
	}
	protected String getTID() {
		return TID;
	}
	private void setTID(String tID) {
		TID = tID;
	}
	protected byte[] getFulldata() {
		return fulldata;
	}
	private void setFulldata(byte[] fulldata) {
		this.fulldata = fulldata;
	}
	private Input input;
       private Output output;
       private String TID;
       private byte[] fulldata;
       protected Transaction(Input input,Output output) throws NoSuchAlgorithmException {
    	     this.input=input;//TODO take care of this/inspect properly
    	     this.output=output;
    	     byte[] arr1=input.getData();
    	     byte[] arr2=output.getData();
    	     //TODO deposit rest to Miner ( Very Important )
    	     fulldata=new byte[arr1.length+arr2.length];
    	     for(int i=0;i<arr1.length;i++)
    	    	 fulldata[i]=arr1[i];
    	     for(int i=0;i<arr2.length;i++)
    	    	 fulldata[arr1.length+i]=arr2[i];
    	     MessageDigest md = MessageDigest.getInstance("SHA-256");  
    	     TID=Util.parseByteToHex(md.digest(fulldata));
       }
       protected static Transaction getTransaction(File f) throws IOException, InvalidOutputException, InvalidInputException, NoSuchAlgorithmException {
    	   if(f.exists()){
    		   Input input=Input.read(f,0);
    		   Output output=Output.read(f,input.getData().length);
    		   return new Transaction(input,output);
    	   }
    	   else{
    		   throw new FileNotFoundException();
    	   }
       }
       public void saveToFile() throws IOException {
    	   String name=TID+".dat";
		   FileOutputStream fis = new FileOutputStream(new File(name));
		   fis.write(fulldata);
		   fis.close();
       }
       public void getReceipt() throws IOException {
    	   System.out.println("Transaction ID: "+TID+"\n\n");
    	   input.print();
    	   output.print();
       }
       protected int getSize() {
    	   return fulldata.length;
       }
       protected byte[] getData(){
    	   return fulldata;
       }
       public static void main(String args[]) throws NoSuchAlgorithmException, IOException, InvalidOutputException, InvalidInputException {
    	    File f=new File("010.dat");
    	    Scanner sc=new Scanner(System.in);
    		System.out.println("Enter number of inputs");
    		int p1=Integer.parseInt(sc.nextLine());
    	    String[] p2=new String[p1];
    	    int[] p3=new int[p1];
    	    String p4[]=new String[p1];
    	    for(int i=0;i<p1;i++) {
    	    	System.out.println("Enter TID");
    	    	p2[i]=sc.nextLine();
    	    	System.out.println("Enter output Index");
    	    	p3[i]=Integer.parseInt(sc.nextLine());
    	    	System.out.println("Enter Siganture");
    	    	p4[i]=sc.nextLine();
    	    }
    	    int p5;
    	    System.out.println("Enter number of outputs");
    	    p5=Integer.parseInt(sc.nextLine());
    	    long p6[]=new long[p5];
    	    String p7[]=new String[p5];
    	    for(int j=0;j<p5;j++) {
    	    	System.out.println("Enter number of coins");
    	    	p6[j]=Long.parseLong(sc.nextLine());
    	    	System.out.println("Enter directory of public Key");
    	    	p7[j]=sc.nextLine();
    	    }
    	    Transaction t2=new Transaction(new Input(p1,p2,p3,p4),new Output(p5,p6,p7));
    	    t2.saveToFile();
    	    t2.getReceipt();
       }
       protected boolean verifyExist() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, SignatureException {
    	   long inputcoins=0;
    	   long outputcoins=0;
    	   int n=input.getNumberofInputs();
    	   TreeSet<Link> s=new TreeSet<>();
    	   EncryptionClient enc=new EncryptionClient();
    	     for(int i=0;i<n;i++) {
    	    	 String key1=input.getID(i);
    	    	 int val=input.getOutputIndex(i);
    	    	 Link l=new Link(key1,val);
    	    	 if(Server.unusedOutputs.containsKey(l))
    	    	 {
    	    		 if(s.contains(l))
    	    			 return false;
    	    		 else
    	    		 {
    	    			 s.add(l);
    	    			 Output p=Server.unusedOutputs.get(l);
    	    			 byte[] arr=p.getData();
    	    			 MessageDigest md = MessageDigest.getInstance("SHA-256");  
    	        	     byte[] hash=(md.digest(arr));
    	        	     byte[] signingdata=new byte[68];
    	        	     byte[] hash1=Util.parseHexToByte(key1);
    	        	     
    	        	     for(int j=0;j<32;j++) {
    	        	    	 signingdata[j]=hash1[j];
    	        	     }
    	        	     byte[] more=Util.toByte(val);
    	        	     for(int j=0;j<4;j++) {
    	        	    	 signingdata[32+j]=more[j];
    	        	     }
    	        	     for(int j=0;j<32;j++) {
    	        	    	 signingdata[32+4+j]=hash[j];
    	        	     }
    	        	     if(!enc.verify(input.getSignature(i),signingdata , p.getKey(val)))
    	        	       return false;
    	        	     inputcoins+=p.getCoins(val);
    	    		 }
    	    	 }
    	    	 
    	     }
    	     outputcoins+=output.getCoins();
    	     if(inputcoins<=outputcoins)
    	
    	     return true;
    	     else 
    	    	 return false;
       } 
       
      
}
