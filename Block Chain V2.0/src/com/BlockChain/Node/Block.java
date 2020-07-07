package com.BlockChain.Node;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;

import java.util.Arrays;

import com.Util;

public class Block {
	private volatile int index;
	private volatile String pHash;
	private volatile int count;
	private volatile byte[] header;
	private volatile Transaction[] transactions;
	private volatile byte[] data;
	private volatile String target;
	private volatile long nonce;
	private volatile long timestamp;
	private volatile boolean bah=true;
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
		MessageDigest md=MessageDigest.getInstance("SHA-256");
		byte ind[]=Util.toByte(index);
		byte p[]=Util.parseHexToByte(pHash);
		byte bhash[]=md.digest(data);
		byte targ[]=Util.parseHexToByte(target);
		header=new byte[116];
		for(int i=0;i<4;i++)
			header[i]=ind[i];
		for(int i=0;i<32;i++)
			header[4+i]=p[i];
		for(int i=0;i<32;i++)
			header[4+32+i]=bhash[i];
		for(int i=0;i<32;i++)
			header[4+32+32+i]=targ[i];
	}
	protected Block(byte[] duoChara) throws  InvalidInputException, IOException, InvalidOutputException, NoSuchAlgorithmException ,IndexOutOfBoundsException{
		try {
			header=Arrays.copyOfRange(duoChara, 0, 116);
			data=Arrays.copyOfRange(duoChara, 116, duoChara.length);
			index=Util.toInt(Arrays.copyOfRange(duoChara, 0, 4));
			pHash=Util.parseByteToHex(Arrays.copyOfRange(duoChara, 4,4+32));
			target=Util.parseByteToHex(Arrays.copyOfRange(duoChara,4+32+32,4+32+32+32));
			timestamp=Util.toLong(Arrays.copyOfRange(duoChara, 4+32+32+32, 4+32+32+32+8));
			nonce=Util.toLong(Arrays.copyOfRange(duoChara,4+32+32+32+8,4+32+32+32+8+8));
			count=Util.toInt(Arrays.copyOfRange(duoChara, 116, 116+4));
			transactions=new Transaction[count];
			int ind=116+4;
			for(int i=0;i<count;i++) {
				int size=Util.toInt(Arrays.copyOfRange(duoChara,ind,ind+4));
				ind+=4;
				transactions[i]=Transaction.getTransaction(Arrays.copyOfRange(duoChara,ind,ind+size));
				ind+=size;
			}
			//TODO check if header has been calculated properly
			byte[] currHash=(Arrays.copyOfRange(duoChara,4+32,4+32+32));
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] actHash=md.digest(data);
			if(currHash.length!=actHash.length){
				bah=false;
			}
			else {
				for(int i=0;i<currHash.length;i++) {
					if(currHash[i]!=actHash[i])
						bah=false;
				}
			}
			if(bah==true){
				byte[] c=md.digest(header);
				if(Util.cmpHex(Util.parseByteToHex(c), target)>0)
					bah=false;
			}
		}
		catch(IndexOutOfBoundsException e) {
			bah=false;
			e.printStackTrace();
			System.out.println("Something seriously wrong with binary data");
		}
		catch( InvalidInputException e) {
			e.printStackTrace();
			System.out.println("BLock with Invlaid Input Binary Data recieved");
		}
		catch( InvalidOutputException e) {
			e.printStackTrace();
			System.out.println("BLock with Invlaid Input Binary Data recieved");
		}
	}
	protected byte[] getHeader() {
		return header;
	}
	protected Transaction[] getTransactions() {
		return transactions;
	}
	protected void setNonce(long non) {
		byte[] conv=Util.toByte(non);
		for(int i=0;i<8;i++)
			header[4+32+32+32+8+i]=conv[i];
		nonce=non;
	}
	protected int getIndex() {
		return index;
	}
	protected void setTimestamp(long tm) {
		byte[] conv=Util.toByte(tm);
		for(int i=0;i<8;i++)
			header[4+32+32+32+i]=conv[i];
		timestamp=tm;
	}
	protected byte[] sendHeader() {
		return header;
	}
	protected byte[] getData() {
		return data;
	}
	protected String getPhash() {
		return pHash;
	}
	protected String getTarget() {
		return target;
	}
	protected String getHash() throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return new String(md.digest(header));
	}
	protected boolean getbah() {
		return bah;
	}
}
