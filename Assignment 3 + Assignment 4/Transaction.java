import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
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
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

class Transaction {

	  public static void newTransaction()throws IOException,NoSuchAlgorithmException{
		  try{
			   Scanner sc=new Scanner(System.in);
			   System.out.println("Enter number of inputs");
			   ArrayList < Byte > data=new ArrayList< Byte>();
			   int n=Integer.parseInt(sc.nextLine());
			
			   byte[] inps=toByte(n);//verify once
			   
			   Transaction.append(data,inps);
			  // fis.write(inps);
			   for(int i=0;i<n;i++){
				   System.out.println("Enter Transaction ID (with 0x at beginning ) of output linking to input #"+(i+1));
				   String hexa=sc.nextLine();
				   byte[] transid=parseHexToByte(hexa);//verify once
				   Transaction.append(data,transid);
				   System.out.println("Enter output index of output linking to input #"+(i+1));
				   byte[] ind=(toByte(Integer.parseInt(sc.nextLine())));
				   Transaction.append(data,ind);
				   System.out.println("Enter signature");
				   String sign=sc.nextLine();
				   byte[] signature=sign.getBytes("UTF-8");
				   int len=signature.length;
				   byte[] signlen=(toByte(len));
				   Transaction.append(data,signlen);
				   Transaction.append(data,signature);
			  }
			   System.out.println("Enter number of outputs");
			   n=Integer.parseInt(sc.nextLine());
			   inps=(toByte(n));
			   Transaction.append(data,inps);
			   for(int i=0;i<n;i++){
				   System.out.println("Enter Number of coins.");
				   long coins=Long.parseLong(sc.nextLine());
				   System.out.println(coins);
				   byte[] coin=(toByte(coins));
				   System.out.println(coin.length);
				   System.out.println(toLong(coin));
				   for(int j=0;j<8;j++)
				   {
					  System.out.print(coin[j]+" ");
				   }
				   System.out.println();
				   
				   Transaction.append(data,coin);
				   System.out.println("Enter directory of .pem file");
				   String dir=sc.nextLine();
				   byte[] publicKey=PemUtils.parsePEMFile(new File(dir));
				   int len=publicKey.length;
				   byte[] keyLen=(toByte(len));
				   Transaction.append(data,keyLen);
				   Transaction.append(data, publicKey);
				   
			   }
			   byte[] writableData=new byte[data.size()];
			   for(int i=0;i<data.size();i++){
				   writableData[i]=data.get(i).byteValue();
				   
			   }
			   // Static getInstance method is called with hashing SHA  
		        MessageDigest md = MessageDigest.getInstance("SHA-256");  
		  
		        
		        String name="0x"+parseByteToHex(md.digest(writableData))+".dat"; 
			   FileOutputStream fis = new FileOutputStream(new File(name));
			   fis.write(writableData);
               fis.close();
               System.out.println("Operations Completed");
			   
		  }
		  catch(IOException e){
			  System.out.println("Invalid file name");
		  }
	  }
		public static void checkTransaction(String dir)throws Exception{
			File f=new File(dir);
			if(f.exists()){
				String TID="";
				for(int i=dir.length()-5;i>=0;i--)
				{
					if (!Character.isDigit(dir.charAt(i))&&!Character.isLetter(dir.charAt(i)))
						break;
					TID=dir.charAt(i)+TID;
					
					
				}
				System.out.println("Transaction ID: "+TID+"\n\n");
				
				FileInputStream fis = new FileInputStream(f);
			    BufferedInputStream reader = new BufferedInputStream(fis);
			    byte[] inps=new byte[4];
			    
			    reader.read(inps,0,4);
			    
			    int n=toInt(inps);
			
			    Inputs[] inflow=new Inputs[n];
			    for(int i=0;i<n;i++){
			    	byte[] transid=new byte[32];
			    	reader.read(transid,0,32);
			    	inflow[i]=new Inputs();
			    	inflow[i].ID=parseByteToHex(transid);
			    	byte[] dum=new byte[4];
			    	reader.read(dum,0,4);
			    	inflow[i].index=toInt(dum);
			    	reader.read(dum,0,4);
			    	inflow[i].length=toInt(dum);
			    	int length=toInt(dum);
			    	byte[] sign=new byte[length];
			    	reader.read(sign,0,length);
			    	inflow[i].signature=new String(sign,"UTF-8");
			    }
			    reader.read(inps,0,4);
			    n=toInt(inps);
			
			    Outputs[] ops=new Outputs[n];
			    for(int i=0;i<n;i++)
			    {
			    	ops[i]=new Outputs();
			    	byte[] large=new byte[8];
			    	reader.read(large,0,8);
			    	ops[i].coins=toLong(large);
			    	byte[] len=new byte[4];
			    	reader.read(len,0,4);
			    	ops[i].length=toInt(len);
			    	int llen=ops[i].length;
			    	byte[] pubKey=new byte[llen];
			    	reader.read(pubKey,0,llen);
			    	
			    	StringWriter sw=new StringWriter();
			    	PemWriter now=new PemWriter(sw);
			    	PublicKey pkey=PemUtils.getPublicKey(pubKey, "RSA");
			    	now.writeObject(new PemObject("PUBLIC KEY",pkey.getEncoded()));
			    	now.flush();
			    	String done=sw.toString();
			    	
			    	ops[i].publicKey=done;
			    	now.close();
			    
			    }
			    System.out.println("Number of Inputs "+inflow.length+"\n\n");
			   
			    for(int i=0;i<inflow.length;i++)
			    {
			    	  System.out.println("      Input #"+(i+1));
			    	  System.out.print("               ");
			          System.out.println("Transaction ID : "+ inflow[i].ID);
			    	  System.out.print("               ");
			          System.out.println("Output Index: "+inflow[i].index);
			          System.out.print("               ");
			          System.out.println("Length of Signature: "+inflow[i].length);
			          System.out.print("               ");
			          System.out.println("Signature: "+inflow[i].signature+"\n\n");
			    }
			    System.out.println("Number of Outputs "+ops.length+"\n\n");
			    
			    for(int i=0;i<ops.length;i++)
			    {
			    	  System.out.println("      Output #"+(i+1));
			    	  System.out.print("               ");
			          System.out.println("Number of Coins: "+ops[i].coins);
			          System.out.print("               ");
			          System.out.println("Length of Public Key: "+ops[i].length);
			          System.out.print("               ");
			          String printkey="";
			          printkey="               ";
			          for(int j=0;j<ops[i].publicKey.length();j++)
			          {
			        	  printkey=printkey+ops[i].publicKey.charAt(j);
			        	  if(ops[i].publicKey.charAt(j)=='\n')
			        	  {
			        		  
			        		  printkey=printkey+"               ";               
			        	  }
			          }
			          System.out.println("Public Key: \n"+printkey);
			         
			    }
			    reader.close();
			}
			else
			{
				System.out.println("No such File found");
			}
			
		}
		   
	  
	  public static void append(ArrayList< Byte> li, byte[] arr){
		   for(int i=0;i<arr.length;i++){
			   li.add(arr[i]);
		   }
	  }
	  public static byte[] parseHexToByte(String str){
		  str=str.substring(2,str.length());
		  byte[] val = new byte[str.length() / 2];
		  for (int i = 0; i < val.length; i++) {
			   int index = i * 2;
			   int j = Integer.parseInt(str.substring(index, index + 2), 16);
			   val[i] = (byte) j;
			}
		  return val;
		  
	  }
	  public static String parseByteToHex(byte[] arr){
		    String res="";
		    for(int i=0;i<arr.length;i++)
		    {
		    	res=res+byteToHex(arr[i]);
		    	
		    }
		    return res;
	  }
	  public static String byteToHex(byte num) {
		    char[] hexDigits = new char[2];
		    hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
		    hexDigits[1] = Character.forDigit((num & 0xF), 16);
		    return new String(hexDigits);
		}
	  public static int toInt( byte[] array ) {
		  return
			      ((array[0]   & 0xff) << 24) |
			      ((array[1] & 0xff) << 16) |
			      ((array[2] & 0xff) << 8) |
			       (array[3] & 0xff);
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
			      ((long)(array[7] & 0xff));
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
		  
	  }
	  public static void main(String args[])throws Exception{
		  Security.addProvider(new BouncyCastleProvider());
		//  System.out.println(toByte(1).length);
		  int inp=0;
		  Scanner sc=new Scanner(System.in);
		  do 
		  {
			     System.out.println("Enter 1 for new Transaction.");
			     System.out.println("Enter 2 for checking previous Transacation");
			     System.out.println("Enter anything else to quit");
			     inp=Integer.parseInt(sc.nextLine());
			     switch(inp)
			     {
			     case 1:
			    	 newTransaction();
			    	 break;
			     case 2:
			    	 System.out.println("Enter directory");
			    	 String dir=sc.nextLine();
			    	 checkTransaction(dir);
			    	 break;
			     default:
			        inp=0;
			     
			     }
		  }
		  while(inp!=0);
		  
	  }
	 
}

