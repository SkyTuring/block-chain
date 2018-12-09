package com.fuyq.qkl;

public class TransactionInput {
    /**
     * 引用transactionOutput -> transactionId。
     */
	public String transactionOutputId;
    /**
     * 包含未使用的事务输出。
     */
	public TransactionOutput UTXO;
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
