package com.BlockChain.Node;

public class Inputi {
     protected String transactionID;
     protected int index;
     protected String signature;
     protected String getTransactionID() {
		return transactionID;
	}
	protected int getIndex() {
		return index;
	}
	protected String getSignature() {
		return signature;
	}
	protected void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}
	protected void setIndex(int index) {
		this.index = index;
	}
	protected void setSignature(String signature) {
		this.signature = signature;
	}
	protected Inputi() {
    	 
     }
     protected Inputi(String TID, int index,String signature) {
    	  this.transactionID=TID;
    	  this.index=index;
    	  this.signature=signature;
     }
}
