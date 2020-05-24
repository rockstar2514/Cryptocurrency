import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Block {
       public static void main(String[] args) throws IOException, NoSuchAlgorithmException{
    	   
    	   Scanner sc=new Scanner(System.in);
    	   System.out.println("Enter index of Block");
    	   int index=Integer.parseInt(sc.nextLine());
    	   byte[] ind=Transaction.toByte(index);
    	   System.out.println("Enter hash of Parent");
    	   String pHash=sc.nextLine();
    	   byte[] pData=Transaction.parseHexToByte(pHash);
    	   
    	   System.out.println("Enter target value");
    	  // int target=Integer.parseInt(sc.nextLine(),16);
    	   String targ=sc.nextLine();
    	   byte[] target=Transaction.parseHexToByte(targ);
    	   
    	   System.out.println("Enter directory of Block");
    	   String dir=sc.nextLine();
    	   File f=new File(dir);
    	   FileInputStream fis2 = new FileInputStream(f);
		   BufferedInputStream reader2 = new BufferedInputStream(fis2);
		   byte[] all=reader2.readAllBytes();
		   MessageDigest md = MessageDigest.getInstance("SHA-256");
		   byte[] hash=md.digest(all);
		   byte[] header=new byte[116];
		   for(int i=0;i<ind.length;i++)
		   {
			   header[i]=ind[i];
		   }
		   for(int i=0;i<pData.length;i++)
		   {
			   header[4+i]=pData[i];
		   }
		   for(int i=0;i<hash.length;i++)
		   {
			   header[4+32+i]=hash[i];
		   }
		   for(int i=0;i<target.length;i++)
		   {
			   header[4+32+32+i]=target[i];
		   }
		   int flag=0;
		   long non=0;
		   long t1=System.nanoTime();
		   while(flag==0){
			   long time=System.nanoTime();
			  // System.out.println(non);
			   byte[] curr=Transaction.toByte(time);
			   for(int i=0;i<8;i++)
				   header[4+32+32+32+i]=curr[i];
			   byte[] nonce=Transaction.toByte(non);
			   for(int i=0;i<8;i++)
				   header[4+32+32+32+8+i]=nonce[i];
			   
			   byte[] currHash=md.digest(header);
			   if(cmp(Transaction.parseByteToHex(currHash),targ)<0)
				   {
				       flag=1;
				       System.out.println("TimeStamp:- "+time+" ns since Epoch");
				       System.out.println("Nonce:- "+non);
				       long t2=System.nanoTime();
				       System.out.println("Time to find value:- "+(t2-t1)+"ns");
				   }
			   non++;
			   
		   }
		   
		   reader2.close();
		   sc.close();
       }
       public static int cmp(String s1,String s2){
    	    for(int i=0;i<s1.length();i++)
    	    {
    	    	char ch1=s1.charAt(i);
    	    	char ch2=s2.charAt(i);
    	    	if(ch1>ch2)
    	    		return 1;
    	    	else if(ch2>ch1)
    	    		return -1;
    	    }
    	    return -1;
       }
}
