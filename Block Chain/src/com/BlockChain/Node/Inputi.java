package com.BlockChain.Node;

public class Inputi {
     public String transactionId;
     public int index;
     public String signature;
     public String getTransactionID() {
		return transactionId;
	}
	public int getIndex() {
		return index;
	}
	public String getSignature() {
		return signature;
	}
	public void setTransactionID(String transactionID) {
		this.transactionId = transactionID;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public Inputi() {
    	 
     }
     public Inputi(String TID, int index,String signature) {
    	  this.transactionId=TID;
    	  this.index=index;
    	  this.signature=signature;
     }
}
