package com.BlockChain.Node;

public class Outputo {
     protected long amount;
     protected String recipient;
     protected Outputo(){
    	 
     }
     protected Outputo(long a,String rec) {
    	 amount=a;
    	 recipient=rec;
     }
	protected long getAmount() {
		return amount;
	}
	protected String getRecipient() {
		return recipient;
	}
	protected void setAmount(long amount) {
		this.amount = amount;
	}
	protected void setRecipient(String recipient) {
		this.recipient = recipient;
	}
     
}
