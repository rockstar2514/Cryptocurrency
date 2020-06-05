package com.BlockChain.Node;

public class InvalidInputException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
    public void printStackTrace() {
    	 System.out.println("Invalid input");
    }
      
}
