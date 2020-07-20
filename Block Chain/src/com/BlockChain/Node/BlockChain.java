package com.BlockChain.Node;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

import com.Util;
public class BlockChain {
	private Block last;
	private String dir;
	private String target;
	private int size=0;
	protected BlockChain(String d,String target) {
		dir=d;
		this.target=target;
		last=null;
	}
	protected void addBlock(Block b) {	 
		try {Files.write(Paths.get(dir), Util.append(b.getHeader(), b.getData()), StandardOpenOption.APPEND);FileOutputStream out = new FileOutputStream("Block"+size);out.write(Util.append(b.getHeader(), b.getData()));out.close();size++;last=b;} catch (IOException e) {System.out.println("File to print Block Chain to not found . Fuck OFf");System.exit(1);e.printStackTrace();}	
	}
	protected String getTopHash() {
		if(size==0) {
			String s="";
			for(int i=0;i<64;i++)s+="0";
			return s;
		}
		else {
			return last.getHash();
		}
	}
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

