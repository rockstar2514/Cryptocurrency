package com.BlockChain.Node;

 class Link implements Comparable<Link>{
    String TID; int b;
    public Link(String a,int b) {
    	TID=a;
    	this.b=b;
    }
	@Override
	//TODO Provide Encapsulation
	public int compareTo(Link o) {
	
		if(o.TID.contentEquals(TID)) {
			   if(b==o.b)
				   return 0;
			   if(b>o.b)
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
