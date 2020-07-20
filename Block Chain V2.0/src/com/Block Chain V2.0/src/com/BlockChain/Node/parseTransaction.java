package com.BlockChain.Node;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.google.gson.annotations.Expose;

public class parseTransaction {
	  
	  public Inputi[] inputs;
	  
	  public Outputo[] outputs;
	  public String id;
	  public parseTransaction() {
		  
	    }
	  public parseTransaction(String id,Inputi[] inputs,Outputo[] outputs) {
		  this.inputs=inputs;
		  this.outputs=outputs;
		  this.id=id;
	    }
      public Transaction getRealTransaction() throws IOException, InvalidOutputException, NoSuchAlgorithmException, InvalidInputException {
    	     Input input=Input.parseInput(inputs);
    	     Output output=Output.parseOutput(outputs);
    	     return new Transaction(input,output);
        }
      public Inputi[] getInputs() {
		return inputs;
		}
		public Outputo[] getOutputs() {
			return outputs;
		}
		public void setInputs(Inputi[] inputs) {
			this.inputs = inputs;
		}
		public void setOutputs(Outputo[] outputs) {
			this.outputs = outputs;
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public void getDetails() {
	    	  for(int i=0;i<inputs.length;i++) {
	    		  System.out.println(inputs[i].index);
	    		  System.out.println(inputs[i].signature);
	    		  System.out.println(inputs[i].transactionId);
	    	  }
	    	  for(int i=0;i<outputs.length;i++) {
	    		  System.out.println(outputs[i].amount);
	    		  System.out.println(outputs[i].recipient);
	    	  }
	    }
	
}
