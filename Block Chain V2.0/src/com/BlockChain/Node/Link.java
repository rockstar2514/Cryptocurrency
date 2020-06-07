package com.BlockChain.Node;

 class Link implements Comparable<Link>{
    String TID;
    int index;
    public Link(String a,int b) {
    	TID=a;
    	index=b;
    }
	@Override
	//TODO Provide Encapsulation
	public int compareTo(Link o) {
	
		if(o.TID.contentEquals(TID)) {
			if(index==o.index)
				return 0;
			else if(index>o.index)
				return 1;
			else
				return -1;
		}
		else {
			if(o.TID.compareTo(TID)>0)
				return 1;
			else return -1;
		}
		
		
	}
	 

}
