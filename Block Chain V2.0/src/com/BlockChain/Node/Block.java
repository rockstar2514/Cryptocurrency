package com.BlockChain.Node;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;

import com.Util;

public class Block {
	  int index;
	  String pHash;
      int count;
      byte[] header;
      Transaction[] transactions;
      byte[] data;
      String target;
      long nonce;
      long timestamp;
      //TODO figure out how to get data from another naode and convert to Block
      public Block(int index,String Phash,Transaction transactions[],String target) throws NoSuchAlgorithmException {
    	  this.pHash=Phash;
    	  this.target=target;
    	  this.index=index;
    	  this.transactions=new Transaction[transactions.length];
    	  count=transactions.length;
    	  ArrayList< Byte > dat=new ArrayList<Byte>();
    	  byte[] c=Util.toByte(count);
    	  Util.append(dat, c);
    	  for(int i=0;i<transactions.length;i++) {
    		  this.transactions[i]=transactions[i];
    		  int size=transactions[i].getSize();
    		  c=Util.toByte(size);
    		  Util.append(dat, c);
    		  Util.append(dat, transactions[i].getData());
    	  }
    	  data=new byte[dat.size()];
	      for(int i=0;i<data.length;i++) {
			      data[i]=dat.get(i);
		  }
	      getHeader();
      }
      private void getHeader() throws NoSuchAlgorithmException {
    	  header=new byte[116];
    	  byte[] c=Util.toByte(index);
    	  for(int i=0;i<c.length;i++) {
    		  header[i]=c[i];
    	  }
    	  c=Util.parseHexToByte(pHash);
    	  for(int i=0;i<c.length;i++) {
    		  header[4+i]=c[i];
    	  }
    	  MessageDigest md = MessageDigest.getInstance("SHA-256");
		  c=md.digest(data);
		  for(int i=0;i<c.length;i++)
		   {
			   header[4+32+i]=c[i];
		   }
		  c=Util.parseHexToByte(target);
		  for(int i=0;i<c.length;i++)
		   {
			   header[4+32+32+i]=c[i];
		   }
		   mineBlock();
      }
      private void mineBlock() throws NoSuchAlgorithmException {
    	   
    	   byte flag=0;
    	   long non=0;
    	   MessageDigest md = MessageDigest.getInstance("SHA-256");
    	   while(flag==0) {
    		   Instant now=Instant.now();
    		   long time=now.getEpochSecond()*1000+((long)now.getNano());
    		   byte[] curr=Util.toByte(time);
    		   for(int i=0;i<8;i++)
				   header[4+32+32+32+i]=curr[i];
    		   byte[] nonce=Util.toByte(non);
    		   for(int i=0;i<8;i++)
				   header[4+32+32+32+8+i]=nonce[i];
    		   byte[] currHash=md.digest(header);
			   if(Util.cmpHex(Util.parseByteToHex(currHash),target)<0)
				   {
				       timestamp=time;
				       flag=1;
				   }
			   non++;
    	   }
    	   non--;
    	   nonce=non;
    	   
      }
      protected byte[] sendHeader() {
    	  return header;
      }
      protected byte[] getData() {
    	  return data;
      }
      
}
