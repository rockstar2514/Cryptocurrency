package com.BlockChain.Node;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.Util;

import spals.shaded.com.google.common.io.BaseEncoding;
public class EncryptionClient {
     private int DEFAULT;
     private Signature clerk; 
	 public EncryptionClient() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		  Security.addProvider(new BouncyCastleProvider()); // Provides for Encryption Algorithms like here RSA PSS
		  DEFAULT = 2514;
		  clerk = Signature.getInstance("SHA256withRSA/PSS");// THis is Algorithm
		  clerk.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));//return Default client
	}
	public KeyPair getKeyPair(String Algorithm, int size) throws NoSuchAlgorithmException, NoSuchProviderException {
		Security.addProvider(new BouncyCastleProvider());
		SecureRandom random = new SecureRandom();
		KeyPairGenerator generator = KeyPairGenerator.getInstance(Algorithm, "BC");//If Incorrect Algorithm error will be thrown
        generator.initialize(size, random);//key size
        KeyPair pair = generator.generateKeyPair();
        return pair;
	}
	public boolean verify(String encryptedtexthdump,String originaltext,PublicKey pubKey) throws InvalidKeyException, SignatureException, UnsupportedEncodingException {
		Security.addProvider(new BouncyCastleProvider());
		clerk.initVerify(pubKey);
		clerk.update(Util.parseHexToByte(originaltext));
		encryptedtexthdump=encryptedtexthdump.toLowerCase();
		byte[] enc = BaseEncoding.base16().lowerCase().decode(encryptedtexthdump);
		return clerk.verify(enc);
	}
	public String sign(String original,PrivateKey privKey) throws InvalidKeyException, SignatureException, UnsupportedEncodingException {
		Security.addProvider(new BouncyCastleProvider());
		clerk.initSign(privKey);
		clerk.update(Util.parseHexToByte(original));
		byte[] arr=clerk.sign();
		return BaseEncoding.base16().lowerCase().encode(arr);
	}
	public boolean verify(String encryptedtexthdump,byte[] original,PublicKey pubKey) throws InvalidKeyException, SignatureException {
		Security.addProvider(new BouncyCastleProvider());
		clerk.initVerify(pubKey);
		clerk.update(original);
		encryptedtexthdump=encryptedtexthdump.toLowerCase();
		byte[] enc = Util.parseHexToByte((encryptedtexthdump));
		return clerk.verify(enc);
	}

}
