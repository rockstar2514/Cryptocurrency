package com;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;



public class Util {
	 
	  public static void append(ArrayList< Byte> li, byte[] arr){
		   for(int i=0;i<arr.length;i++){
			   li.add(arr[i]);// Appends at the end of Array List
		   }
	  }
	  public static byte[] parseHexToByte(String str){
		  if(str.substring(0,2).contentEquals("0x"))
		  str=str.substring(2,str.length());
		  byte[] val = new byte[str.length() / 2];
		  for (int i = 0; i < val.length; i++) {
			   int index = i * 2;
			   int j = Integer.parseInt(str.substring(index, index + 2), 16);
			   val[i] = (byte) j;
			}
		  return val;//Converts a Hexadecimal number to Binary Representation
		  
	  }
	  public static String parseByteToHex(byte[] arr){
		    String res="";
		    for(int i=0;i<arr.length;i++)
		    {
		    	res=res+byteToHex(arr[i]);
		    	
		    }
		    return res;//Converts Byte representation to String Hexadecimal
	  }
	  public static String byteToHex(byte num) {
		    char[] hexDigits = new char[2];
		    hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
		    hexDigits[1] = Character.forDigit((num & 0xF), 16);
		    return new String(hexDigits);// Auxiliary Func for parseByteToHex
		}
	  public static int toInt( byte[] array ) {
		  return
			      ((array[0]   & 0xff) << 24) |
			      ((array[1] & 0xff) << 16) |
			      ((array[2] & 0xff) << 8) |
			       (array[3] & 0xff);//converts byte array to int;
		  }
	  public static long toLong( byte[] array ) {
		  return
			      ((long)(array[0]   & 0xff) << 56) |
			      ((long)(array[1] & 0xff) << 48) |
			      ((long)(array[2] & 0xff) << 40) |
			      ((long)(array[3] & 0xff) << 32) |
			      ((long)(array[4] & 0xff) << 24) |
			      ((long)(array[5] & 0xff) << 16) |
			      ((long)(array[6] & 0xff) << 8) |
			      ((long)(array[7] & 0xff));//convets byte array to long
		  }
	  public static byte[] toByte(int n)
	  {
		  byte[] result=(BigInteger.valueOf(n)).toByteArray();
		  if(result.length<4)
		  {
			  byte[] alt=new byte[4];
			  for(int i=0;i<result.length;i++)
			  {
				  alt[4-i-1]=result[result.length-i-1];
			  }
			  return alt;
		  }
		  else
		  {
			  return result;
		  }
		  // function to convert int to byte array
		  
	  }
	  public static byte[] toByte(long n)
	  {
		  byte[] result=(BigInteger.valueOf(n)).toByteArray();
		  if(result.length<8)
		  {
			  byte[] alt=new byte[8];
			  for(int i=0;i<result.length;i++)
			  {
				  alt[8-i-1]=result[result.length-i-1];
			  }
			  return alt;
		  }
		  else
		  {
			  return result;
		  }
		  // function to convert long to byte array
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
	    	 return out;//Hexadecimal String to byte array(not binary representaion)
	     }
	     public static int hexToBin(char ch)
	     {
	    	 if('0'<=ch&&ch<='9') return ch-'0';
	    	 if('A'<=ch&&ch<='F')return ch-'A'+10;
	    	 if('a'<=ch&&ch<='f')return ch-'a'+10;
	    	 return -1;//Auxuliary Function for hexSToBin();
	     }
	     public static boolean isHex(String a) {
	    	 
	    	 for(int i=0;i<a.length();i++) {
	    		 int val=a.codePointAt(i);//check this need to improve maybe
	    		 if(!((val>='a'&&val<='f')||(val>='0'&&val<='9')||(val>='A'&&val<='F')))//may not work
	    			 return false;
	    	 }
	    	 return true;
	     }
	  public static byte[] parsePEMFile(File pemFile) throws IOException {
	        if (!pemFile.isFile() || !pemFile.exists()) {
	            throw new FileNotFoundException(String.format("The file '%s' doesn't exist.", pemFile.getAbsolutePath()));
	        }
	        PemReader reader = new PemReader(new FileReader(pemFile));
	        PemObject pemObject = reader.readPemObject();
	        byte[] content = pemObject.getContent();
	        reader.close();
	        return content;//return byte content of pem file
	    }

	    public static PublicKey getPublicKey(byte[] keyBytes, String algorithm) {
	        PublicKey publicKey = null;
	        try {
	            KeyFactory kf = KeyFactory.getInstance(algorithm);
	            EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
	            publicKey = kf.generatePublic(keySpec);
	        } catch (NoSuchAlgorithmException e) {
	            System.out.println("Could not reconstruct the public key, the given algorithm could not be found.");
	        } catch (InvalidKeySpecException e) {
	            System.out.println("Could not reconstruct the public key");
	        }

	        return publicKey;//constructs public key from data fetched by parsePemFile 
	    }

	    public static PrivateKey getPrivateKey(byte[] keyBytes, String algorithm) {
	        PrivateKey privateKey = null;
	        try {
	            KeyFactory kf = KeyFactory.getInstance(algorithm);
	            EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
	            privateKey = kf.generatePrivate(keySpec);
	        } catch (NoSuchAlgorithmException e) {
	            System.out.println("Could not reconstruct the private key, the given algorithm could not be found.");
	        } catch (InvalidKeySpecException e) {
	            System.out.println("Could not reconstruct the private key");
	        }

	        return privateKey;//constructs Private Key from Data by parsePemFile()
	    }

	    public static PublicKey readPublicKeyFromFile(String filepath, String algorithm) throws IOException {
	        byte[] bytes = Util.parsePEMFile(new File(filepath));
	        return Util.getPublicKey(bytes, algorithm);//direct function
	    }

	    public static PrivateKey readPrivateKeyFromFile(String filepath, String algorithm) throws IOException {
	        byte[] bytes = Util.parsePEMFile(new File(filepath));
	        return Util.getPrivateKey(bytes, algorithm);//direct function
	    }
	    
}
