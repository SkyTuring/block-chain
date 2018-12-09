package com.fuyq.qkl;

import com.fuyq.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * 封装区块对象
 */
public class Block {

    public boolean isFaxing;

	public String hash;
    /**
     * 上一个区块的hash值
     */
	public String previousHash;
    /**
     * 每个区块存放的信息
     */
	public String merkleRoot;
    /**
     * 时间戳
     */
	public Date timeStamp;
    /**
     * 挖矿者的工作量证明
     */
	public int nonce;
    /**
     * 我们的交易信息最后会转化成merkle Root
     */
	public ArrayList<Transaction> transactions = new ArrayList<>();

	public Block(String previousHash) {
		this.previousHash = previousHash;
		this.timeStamp = new Date();
		//根据previousHash、data和timeStamp产生唯一hash
		this.hash = calculateHash(); 
	}

    /**
     * 基于上一块的内容计算新的散列
     */
	public String calculateHash() {
		return StringUtil.applySha256(previousHash
                + Long.toString(timeStamp.getTime()) + Integer.toString(nonce) + merkleRoot);
	}


	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		//目标值，difficulty越大，下面计算量越大
		String target = StringUtil.getDificultyString(difficulty);
		//difficulty如果为5，那么target则为 00000
		while(!hash.substring(0, difficulty).equals(target)) {
			nonce ++;
            System.out.println("nonce:"+nonce);
            hash = calculateHash();
		}
		System.out.println("#info:创建区块:" + hash);
	}

	/**
	 * 将交易添加到区块中
	 */
	public boolean addTransaction(Transaction transaction) {
		//进程事务，检查是否有效，除非block是genesis块，然后忽略。
		if(transaction == null) {
			return false;
		}
		if(!isFaxing) {
			if((!transaction.processTransaction())) {
				System.out.println("#error:交易失败。事务被丢弃。");
				return false;
			}
		}

		transactions.add(transaction);
		System.out.println("#info:事务成功地添加到区块中");
		return true;
	}

}
