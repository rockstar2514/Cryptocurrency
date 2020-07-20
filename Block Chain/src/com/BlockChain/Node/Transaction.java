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

public class Transaction implements Comparable<Transaction>{
	private Input input;
	private Output output;
	private String TID;
	private byte[] fulldata;
	protected Input getInput() {
		return input;
	}
	protected Output getOutput() {
		return output;
	}
	protected String getTID() {
		return TID;
	}
	protected byte[] getFulldata() {
		return fulldata;
	}
	protected Transaction(Input input,Output output) throws NoSuchAlgorithmException {
		this.input=input;
		this.output=output;
		byte[] arr1=null;
		if(input==null)
			arr1=new byte[4];
		else
			arr1=input.getData();
		byte[] arr2=output.getData();
		fulldata=new byte[arr1.length+arr2.length];
		for(int i=0;i<arr1.length;i++)
			fulldata[i]=arr1[i];
		for(int i=0;i<arr2.length;i++)
			fulldata[arr1.length+i]=arr2[i];
		MessageDigest md = MessageDigest.getInstance("SHA-256");  
		TID=Util.parseByteToHex(md.digest(fulldata));
	}
	protected static Transaction getTransaction(byte[] arr) throws InvalidInputException, IOException, InvalidOutputException, NoSuchAlgorithmException {
		Input input=Input.read(arr, 0);
		Output output=Output.read(arr, input.getLengthOfData());
		return new Transaction(input,output);
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
	@Override
	public int compareTo(Transaction o) {
		return TID.compareTo(o.getTID());
	}
}
