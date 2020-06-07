package com.BlockChain.Node;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.ArrayList;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import com.Util;

public class Output {
	  private int outputs;
	  private long coins[];
	  private PublicKey keys[];
	  private byte[] data;
	  protected Output(int outputs,long coins[],PublicKey keys[]) throws IOException, InvalidOutputException {
		    this.outputs=outputs;
		    this.coins=new long[coins.length];
		    for(int i=0;i<coins.length;i++) {
		    	this.coins[i]=coins[i];
		    }
		    this.keys=new PublicKey[keys.length];
		    for(int i=0;i<keys.length;i++) {
		    	this.keys[i]=keys[i];
		    }
		    if(!verify())
		    	throw new InvalidOutputException();
		    setData();
	  }
	  protected Output(int outputs,long coins[],String dir[]) throws IOException, InvalidOutputException {
		  this.outputs=outputs;
		    this.coins=new long[coins.length];
		    for(int i=0;i<coins.length;i++) {
		    	this.coins[i]=coins[i];
		    }
		  keys=new PublicKey[dir.length];
		  for(int i=0;i<dir.length;i++) {
			  String file=dir[i];
			  keys[i]=Util.readPublicKeyFromFile(file, "RSA");
		  }
		  if(!verify())
		    	throw new InvalidOutputException();
		  setData();
	  }
	  private void setData() throws IOException {
		  ArrayList< Byte > dat=new ArrayList< Byte >();
		    byte[] ops=(Util.toByte(outputs));
			Util.append(dat,ops);
			for(int i=0;i<outputs;i++){ 
				   long c=coins[i];
				   byte[] coin=(Util.toByte(c));
				   Util.append(dat,coin);
				   PublicKey pkey=keys[i];
			 	   StringWriter sw=new StringWriter();
			       PemWriter now=new PemWriter(sw);
			       now.writeObject(new PemObject("PUBLIC KEY",pkey.getEncoded()));
			       now.flush();
			       String done=sw.toString();
			       done=done.replaceAll("\r","");//trimming keys.Maybe trimming required when files are read. TODO
			       now.close();
	               byte[] kdata=done.getBytes("UTF-8");//UTF-8 ya normal?? TODO
	               byte[] keyLen=Util.toByte(kdata.length);
				   Util.append(dat,keyLen);
				   Util.append(dat, kdata);
		   }
		   data=new byte[dat.size()];
		   for(int i=0;i<data.length;i++) {
				    data[i]=dat.get(i);
			}
	  }
	  protected static Output read(File f,int offset) throws IOException, InvalidOutputException {
		  FileInputStream fis = new FileInputStream(f);
	 	  BufferedInputStream reader = new BufferedInputStream(fis);
	 	  if(offset!=0)
	 	  {	
	 	   	reader.read(new byte[offset],0,offset);
	 	  }
	 	    byte ops[]=new byte[4];
		    reader.read(ops,0,4);
		    int outputs=Util.toInt(ops);
		    long coins[]=new long[outputs];
		    PublicKey keys[]=new PublicKey[outputs];
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
		    return new Output(outputs,coins,keys);
	 	  
	  }
	  protected void print() throws IOException {
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
	  protected byte[] getData() {
		  return data;
	  }
	  private boolean verify(){
		  //TODO apply check
		  return true;
	  }
	  protected PublicKey getKey(int index) {
		  return keys[index];
	  }
      protected long getCoins(int index) {
    	  return coins[index];
      }
      protected long getCoins() {
    	  long tot=0;
    	  for(int i=0;i<coins.length;i++) {
    		   tot+=coins[i];
    	  }
    	  return tot;
      }
}
