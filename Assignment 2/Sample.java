import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;

import org.bouncycastle.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.signers.*;
import static java.nio.charset.StandardCharsets.*;

public class Sample {
     public static void main(String[] args)throws Exception
     {
    	 Security.addProvider(new BouncyCastleProvider());
    	 int max=javax.crypto.Cipher.getMaxAllowedKeyLength("RSA");
    	 System.out.println(max);
    	 Scanner sc=new Scanner(System.in);
    	 
    	 System.out.println("Enter directory of public key");
    	 String dir=sc.nextLine();
    	 PublicKey pubKey=PemUtils.readPublicKeyFromFile(dir,"RSA");//crosscheck this function;
    	 System.out.println("Enter Encrypted String");

    	 String sign="";
    	
    	 sign=sc.nextLine();
         
    	 System.out.println("Enter Original String");
    	 String original="";
    	 original=sc.nextLine();
    	
    	 byte[] enc = Sample.hexSToBin(sign);
         byte[] ori=original.getBytes("UTF-8");
    	 
    //	 SecureRandom random = new SecureRandom();
         
    	 Signature signee = Signature.getInstance("SHA256withRSA/PSS");
         
    	 signee.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));//tune salt lenghh and trailer
         
         signee.initVerify(pubKey);
         
         signee.update(ori);

         boolean bool=signee.verify(enc);
         if(bool)
        	 System.out.println("Matched");
         else
        	 System.out.println("Failed");
         
     }
     public static byte[] hexSToBin(String s) throws Exception
     {
    	
    	 final int len=s.length();
    	 if(len%2!=0)
    		 throw new Exception("Invalid String");
    	 byte[] out=new byte[len/2];
    	 for(int i=0;i<len;i+=2)
    	 {
    		 int h=hexToBin(s.charAt(i));
    		 int l=hexToBin(s.charAt(i+1));
    		 out[i/2]=(byte)(h*16+l);
    	 }
    	 return out;
     }
     public static int hexToBin(char ch)
     {
    	 if('0'<=ch&&ch<='9') return ch-'0';
    	 if('A'<=ch&&ch<='F')return ch-'A'+10;
    	 if('a'<=ch&&ch<='f')return ch-'a'+10;
    	 return -1;
     }
     //public String printHexBinary();define later
}
