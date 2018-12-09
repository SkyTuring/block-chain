package com.fuyq.qkl;

import com.fuyq.util.StringUtil;

import java.security.PublicKey;

/**
 * 交易输出类将显示从交易中发送给每一方的最终金额。
 * 这些作为新交易中的输入参考，作为证明你可以发送的金额数量。
 */
public class TransactionOutput {
	public String id;
	/**
	 * 这些硬币的新主人
	 */
	public PublicKey reciepient;
	/**
	 * 他们拥有的硬币数量
	 */
	public float value;
	/**
	 * 这个输出创建的事务的id
	 */
	public String parentTransactionId;

	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(
				StringUtil.getStringFromKey(reciepient)+
                        Float.toString(value)+
                        parentTransactionId);
	}

    /**
     * 检查硬币是否属于你
     */
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
}
