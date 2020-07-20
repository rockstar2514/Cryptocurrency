package com.BlockChain.Node;

public class Outputo {
     public long amount;
     public String recipient;
     public Outputo(){
    	 
     }
     public Outputo(long a,String rec) {
    	 amount=a;
    	 recipient=rec;
     }
	public long getAmount() {
		return amount;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
     
}
