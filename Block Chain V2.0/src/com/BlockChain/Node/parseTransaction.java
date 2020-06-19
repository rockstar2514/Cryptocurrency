package com.BlockChain.Node;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class parseTransaction {
	  protected Inputi[] inputs;
	  protected Outputo[] outputs;
	  protected parseTransaction() {
		  
	    }
	  protected parseTransaction(Inputi[] inputs,Outputo[] outputs) {
		  this.inputs=inputs;
		  this.outputs=outputs;
	    }
      protected Transaction getRealTransaction() throws IOException, InvalidOutputException, NoSuchAlgorithmException, InvalidInputException {
    	     Input input=Input.parseInput(inputs);
    	     Output output=Output.parseOutput(outputs);
    	     return new Transaction(input,output);
        }
      protected Inputi[] getInputs() {
		return inputs;
		}
		protected Outputo[] getOutputs() {
			return outputs;
		}
		protected void setInputs(Inputi[] inputs) {
			this.inputs = inputs;
		}
		protected void setOutputs(Outputo[] outputs) {
			this.outputs = outputs;
		}
		protected void getDetails() {
	    	  for(int i=0;i<inputs.length;i++) {
	    		  System.out.println(inputs[i].index);
	    		  System.out.println(inputs[i].signature);
	    		  System.out.println(inputs[i].transactionID);
	    	  }
	    	  for(int i=0;i<outputs.length;i++) {
	    		  System.out.println(outputs[i].amount);
	    		  System.out.println(outputs[i].recipient);
	    	  }
	    }
	
}
