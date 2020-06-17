package com.BlockChain.Node;
import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Scanner;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import com.Util;
@Deprecated
public class TransactionOld {
	//TODO make a transaction directory where all transaction files will be stored
	// private ArrayList < Byte > data;//Entire Byte Data
     private int inputs;//Number of Inputs
     private String TID[];//Transaction ID of Inputs
     private int OutputIndex[];//Output Index of corresponding input
     private String signatures[];//Signatures correspnding to inputs
     private int outputs;//Number of outputs
     private long coins[];//TODO make more classes to distribute code even more
     private String dir[];//Does not always store information
     private String ID;
     private PublicKey keys[];//TODO check on memory consumption once// this is also calculated during file writing 
     private byte fulldata[];
     private TransactionOld() {}
	 private void makeTransaction(int inp,String TID[], int outputind[],String signs[],int ops,long coinlist[],String locs[]) {
		 inputs=inp;
		 this.TID=TID;
		 OutputIndex=outputind;
		 signatures=signs;
		 outputs=ops;
		 coins=coinlist;
		 dir=locs;
	    }
	public static TransactionOld getTrans(int inp,String TID[], int outputind[],String signs[],int ops,long coinlist[],String locs[]) throws NoSuchAlgorithmException, IOException {
		if(inp<=0||ops<=0) {
			System.out.println("Invalid input1");
			return null;
		}
		else
		{
			if(TID.length!=inp||outputind.length!=inp||signs.length!=inp||coinlist.length!=ops||locs.length!=ops) {
				System.out.println("Invalid Input2");
				return null;
			}
			else
			{
				//Check need to be modified later on
				for(int i=0;i<inp;i++) {
					String curr=TID[i];
					if((!Util.isHex(curr))||curr.length()!=64)
					{
						System.out.println(curr);
						System.out.println(curr.length());
						for(int j=0;j<curr.length();j++)
							System.out.println(curr.charAt(j));
						System.out.println("Invalid Input3"+curr.length());
						return null;
					}
				}
				for(int i=0;i<inp;i++) {
					int val=outputind[i];
					if(val<0)
					{
						System.out.println("Invalid Input4");
						return null;
					}
				}
				for(int i=0;i<inp;i++) {
					String sigs=signs[i];
					if(!(Util.isHex(sigs)))
					{
						System.out.println("Invalid Input5");
						return null;
					}
				}
				for(int i=0;i<ops;i++) {
					long c=coinlist[i];
					if(c<0)
					{
						System.out.println("Invalid Input6");
						return null;
					}
				}
				// verify directories as well
				TransactionOld s=new TransactionOld();
				s.makeTransaction(inp, TID, outputind, signs, ops, coinlist, locs);
				s.writeToFile();
				return s;
			}
		}
	}
	public static TransactionOld getTransaction(String path) throws NoSuchAlgorithmException, IOException {
		  File f=new File(path);
		  if(f.exists())
		  {
			  TransactionOld t=new TransactionOld();
			  t.readTransaction(path);
			  return t;
		  }
		  else
		  {
			  System.out.println("Wrong file name");
			  return null;
		  }
	}
	private void writeToFile() throws IOException, NoSuchAlgorithmException
	{
		   ArrayList<Byte> data=new ArrayList<Byte>();
		   
		   byte[] inps=Util.toByte(inputs);//verify once
		   //TODO manage exceptions
		   
		   Util.append(data,inps);
		   for(int i=0;i<inputs;i++){
			   String hexa=TID[i];
			   byte[] transid=Util.parseHexToByte(hexa);//verify once
			   Util.append(data,transid);
			   byte[] ind=(Util.toByte(OutputIndex[i]));
			   Util.append(data,ind);
			   String sign=signatures[i];
			   byte[] signature=Util.parseHexToByte(sign);
			   int len=signature.length;
			   byte[] signlen=(Util.toByte(len));
			   Util.append(data,signlen);
			   Util.append(data,signature);
		  }
		   byte[] ops=(Util.toByte(outputs));
		   Util.append(data,ops);
		   keys=new PublicKey[outputs];
		   for(int i=0;i<outputs;i++){
			   long c=coins[i];
			   byte[] coin=(Util.toByte(c));
			   Util.append(data,coin);
			   String loc=dir[i];//TODO apply a check over here
			  // System.out.println(dir[i]);
			   PublicKey pkey=Util.readPublicKeyFromFile(loc, "RSA");
			   keys[i]=pkey;
			   StringWriter sw=new StringWriter();
		       PemWriter now=new PemWriter(sw);
		       now.writeObject(new PemObject("PUBLIC KEY",pkey.getEncoded()));
		       now.flush();
		       String done=sw.toString();
		       done=done.replaceAll("\r","");//trimming keys.Maybe trimming required when files are read. TODO
		       now.close();
               byte[] kdata=done.getBytes("UTF-8");//UTF-8 ya normal?? TODO
               byte[] keyLen=Util.toByte(kdata.length);
			   Util.append(data,keyLen);
			   Util.append(data, kdata);
		   }
		    fulldata=new byte[data.size()];
		   for(int i=0;i<data.size();i++){
			   fulldata[i]=data.get(i).byteValue();
		   }
	        MessageDigest md = MessageDigest.getInstance("SHA-256");  
	        ID=Util.parseByteToHex(md.digest(fulldata)); 
	        String name=ID+".dat";
		    FileOutputStream fis = new FileOutputStream(new File(name));
		    fis.write(fulldata);
            fis.close();
	}
	private void readTransaction(String fileName) throws IOException, NoSuchAlgorithmException {
		//TODO make changes here when universal directory is made
		File f=new File(fileName);
		FileInputStream fis = new FileInputStream(f);
	    BufferedInputStream reader = new BufferedInputStream(fis);
	    byte[] inps=new byte[4];
	    reader.read(inps,0,4);
	    inputs=Util.toInt(inps);
	    TID=new String[inputs];
	    OutputIndex=new int[inputs];
	    signatures=new String[inputs];
	    for(int i=0;i<inputs;i++){
	    	byte[] transid=new byte[32];
	    	reader.read(transid,0,32);
	    	TID[i]=Util.parseByteToHex(transid);
	    	byte[] dum=new byte[4];
	    	reader.read(dum,0,4);
	    	//inflow[i].index=toInt(dum);
	    	OutputIndex[i]=Util.toInt(dum);
	    	reader.read(dum,0,4);
	    	//inflow[i].length=toInt(dum);
	    	int length=Util.toInt(dum);
	    	byte[] sign=new byte[length];
	    	reader.read(sign,0,length);
	    	signatures[i]=Util.parseByteToHex(sign);
	    }
	    byte ops[]=new byte[4];
	    reader.read(ops,0,4);
	    outputs=Util.toInt(ops);
	    coins=new long[outputs];
	    keys=new PublicKey[outputs];
	    for(int i=0;i<outputs;i++)
	    {
	    	byte[] large=new byte[8];
	    	reader.read(large,0,8);
	    	coins[i]=Util.toLong(large);
	    	byte[] len=new byte[4];
	    	reader.read(len,0,4);
	    	int llen=Util.toInt(len);
	    	byte[] pubKey=new byte[llen];
	    	reader.read(pubKey,0,llen);
	    	StringReader sr=new StringReader(new String(pubKey));
	    	PemReader r2=new PemReader(sr);
	    	PemObject pemObject=r2.readPemObject();
	    	keys[i]=Util.getPublicKey(pemObject.getContent(),"RSA");//TODO verify this part and also check if Algorithm need generalissation
	    	r2.close();
	    }
	    reader.close();
		FileInputStream fis2 = new FileInputStream(f);
	    BufferedInputStream reader2 = new BufferedInputStream(fis2);
	    byte[] all=reader2.readAllBytes();
        MessageDigest md = MessageDigest.getInstance("SHA-256");  
        ID=Util.parseByteToHex(md.digest(all)); 
	    reader2.close();
	}
	public void getReceipt() throws IOException//TODO determine appopriate accessor level
	{
		System.out.println("Transaction ID: "+ID+"\n\n");
	    System.out.println("Number of Inputs "+inputs+"\n\n");
	   
	    for(int i=0;i<inputs;i++)
	    {
	    	  System.out.println("      Input #"+(i+1));
	    	  System.out.print("               ");
	          System.out.println("Transaction ID : "+ TID[i]);
	    	  System.out.print("               ");
	          System.out.println("Output Index: "+OutputIndex[i]);
	          System.out.print("               ");
	          System.out.println("Length of Signature: "+signatures[i].length()/2);//vefiy once again
	          System.out.print("               ");
	          System.out.println("Signature: "+signatures[i]+"\n\n");
	    }
	    System.out.println("Number of Outputs "+outputs+"\n\n");
	    for(int i=0;i<outputs;i++)
	    {
	    	  System.out.println("      Output #"+(i+1));
	    	  System.out.print("               ");
	          System.out.println("Number of Coins: "+coins[i]);
	          System.out.print("               ");
	          StringWriter sw=new StringWriter();
		      PemWriter now=new PemWriter(sw);
		      now.writeObject(new PemObject("PUBLIC KEY",keys[i].getEncoded()));
		      now.flush();
		      String done=sw.toString();
		      now.close();
		      done=done.replaceAll("\r", "");
	          System.out.println("Length of Public Key: "+done.length());
	          System.out.println("Public Key: \n"+done);//TODO test all this shit
	    }
	}
	public static void main(String args[]) throws NoSuchAlgorithmException, IOException
	{
		Scanner sc=new Scanner(System.in);
		String s=sc.nextLine();
		TransactionOld t=getTransaction(s);
		t.getReceipt();
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
	    TransactionOld t2=getTrans(p1,p2,p3,p4,p5,p6,p7);
	    t2.getReceipt();
	}
	 
}
