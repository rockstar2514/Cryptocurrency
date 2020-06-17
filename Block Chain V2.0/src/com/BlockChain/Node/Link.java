package com.BlockChain.Node;

 class Link implements Comparable<Link>{
    String TID;
    public Link(String a,int b) {
    	TID=a;
    }
	@Override
	//TODO Provide Encapsulation
	public int compareTo(Link o) {
	
		if(o.TID.contentEquals(TID)) {
				return 0;
		}
		else {
			if(o.TID.compareTo(TID)>0)
				return 1;
			else return -1;
		}
		
		
	}
	 

}
