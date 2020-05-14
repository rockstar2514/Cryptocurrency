package iitkbucks;

import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Scanner;
import org.bouncycastle.util.io.pem.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import spals.shaded.com.google.common.io.BaseEncoding;

public class Produce {
     public static void main(String [] args)throws Exception
     {
    	 Security.addProvider(new BouncyCastleProvider());
    	 Scanner sc=new Scanner(System.in);
    	 System.out.println("Enter directory to save encrypted ");
    	 String dir1=sc.nextLine();
    	 System.out.println("Enter directory to save unecrypted ");
    	 String dir2=sc.nextLine();
    	 System.out.println("Enter directory to save Key ");
    	 String dir3=sc.nextLine();
     	System.out.println("Enter String o encrypt.");
     	String s=sc.nextLine();
     	 SecureRandom random = new SecureRandom();
        Signature signee = Signature.getInstance("SHA256withRSA/PSS");
        signee.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(4096, random);
        KeyPair pair = generator.generateKeyPair();
        PublicKey pubKey = pair.getPublic();
        PrivateKey privKey = pair.getPrivate();
        signee.initSign(privKey);
        signee.update(s.getBytes("UTF-8"));
        byte[] get=signee.sign();
        FileWriter f=new FileWriter(dir1);
        f.write(BaseEncoding.base16().encode(get));
        FileWriter f2=new FileWriter(dir2);
        f2.write(s);
        FileWriter f3=new FileWriter(dir3);
       
        PemWriter real=new PemWriter(f3);
     	real.writeObject(new PemObject("Key",pubKey.getEncoded()));
     	real.close();
     	f.close();
     	//f1.close();
     	f2.close();
     	System.out.println("At the end");//working pretty fine
     }
}
