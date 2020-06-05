package com.BlockChain.Node;

public class InvalidOutputException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
    public void printStackTrace() {
    	 System.out.println("Invalid Output");
    }

}
