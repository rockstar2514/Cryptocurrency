package com.BlockChain.Node;
import com.Util;

import spals.shaded.com.google.common.io.BaseEncoding;

public class cracking {
   public static void main(String[] args) throws Exception {
	   String input="Ma Chudao";
	   System.out.println(BaseEncoding.base64().encode((input.getBytes())));
   }
}
