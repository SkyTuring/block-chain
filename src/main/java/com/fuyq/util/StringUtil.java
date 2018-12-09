package com.fuyq.util;
import com.fuyq.qkl.Transaction;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
/**
 * 工具类
 * 创建数字签名、返回JSON格式数据、返回难度字符串目标
 */
public class StringUtil {

    /**
     * 将Sha256应用到一个字符串并返回结果
     */
	public static String applySha256(String input){

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(input.getBytes("UTF-8"));

			StringBuilder hexString = new StringBuilder();
            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getJson(Object o) {
		return new GsonBuilder()
				.setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setPrettyPrinting().setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        if ("genesisTransaction".equals(f.getName())
                                || "signature".equals(f.getName())
                                || "withCompression".equals(f.getName())
                                || "algorithm".equals(f.getName())){
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create()
                .toJson(o);
	}

	/**
	 *  返回难度字符串目标，与散列比较。难度5将返回“00000”
	 */
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}

	/**
	 * @param privateKey 付款人的私钥
	 * @param input 需要加密的数据信息
	 * @return 签名后返回字节数组。
	 */
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output;
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
            output = dsa.sign();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}

	/**
	 * @param publicKey 公钥
	 * @param data 加密的数据
	 * @param signature 签名
	 * @return 签名是否是有效
	 */
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ECDSAVerify = Signature.getInstance("ECDSA", "BC");
			ECDSAVerify.initVerify(publicKey);
			ECDSAVerify.update(data.getBytes());
			return ECDSAVerify.verify(signature);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}



	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/**
	 * 防止一个区块中存在过多的交易二导致大量的hash计算，
	 * 所以我们将交易计算出merkleRoot
	 * @param transactions 交易列表
	 * @return
	 */
	public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();

		List<String> previousTreeLayer = new ArrayList<>();
		for(Transaction transaction : transactions) {
			previousTreeLayer.add(transaction.transactionId);
		}
		List<String> treeLayer = previousTreeLayer;

		while(count > 1) {
			treeLayer = new ArrayList<>();
			for(int i=1; i < previousTreeLayer.size(); i+=2) {
				treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}
        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
	}


}
