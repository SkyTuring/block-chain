package com.fuyq.qkl;
import com.fuyq.util.StringUtil;

import java.security.*;
import java.util.ArrayList;
/**
 * 每笔交易将携带一定以下信息：
 * 	资金付款人的公匙信息。
 *	资金收款人的公匙信息。
 *	被转移资金的金额。
 *	输入，它是对以前的交易的引用，证明发送者有资金发送。
 *	输出，显示交易中收款方相关地址数量。(这些输出被引用为新交易的输入)
 *	一个加密签名，证明该交易是由地址的发送者是发送的，并且数据没有被更改。(阻止第三方机构更改发送的数量)
 */
public class Transaction {

    /**
     * 包含事务的散列
     */
	public String transactionId;
	/**
     *发送者地址/公钥。
     */
	public PublicKey sender;
    /**
     * 收件人地址/公钥
     */
	public PublicKey reciepient;
    /**
     * 我们希望发送给收件人的金额
     */
	public float value;
    /**
     *加密签名，证明该交易是由地址的发送者是发送的，并且数据没有被更改
     */
	private byte[] signature;

	public ArrayList<TransactionInput> inputs = new ArrayList<>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    /**
     * 粗略计算已经生成了多少事务
     */
	private static int sequence = 0;


	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}

	/**
	 * 处理交易
	 * @return
	 */
	public boolean processTransaction() {
		if(!verifySignature()) {
			System.out.println("#error:事务签名未能验证");
			return false;
		}
		//收集交易输入(确保它们是未使用的)
		if (null != inputs) {
			for(TransactionInput i : inputs) {
                i.UTXO = BlockChain.getInstance().getUTXOs().get(i.transactionOutputId);
            }
		}
		//检查交易是否有效
		if(getInputsValue() < BlockChain.minimumTransaction) {
			System.out.println("#info:交易金额太小: " + getInputsValue());
			System.out.println("#info:请输入大于:" + BlockChain.minimumTransaction+"的金额");
			return false;
		}
		//生成事务输出:
		//得到输入的值，然后是剩余的变化:
		float leftOver = getInputsValue() - value;
		transactionId = calulateHash();
		//值发送给收件人
		outputs.add(new TransactionOutput(this.reciepient, value,transactionId));
		//将剩余的“金额”发送回发送方。
		outputs.add(new TransactionOutput(this.sender, leftOver,transactionId));

		//将输出添加到未使用的列表
		for(TransactionOutput o : outputs) {
            BlockChain.getInstance().getUTXOs().put(o.id,o);
		}
		//从UTXO列表中删除事务输入:
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) {
				continue;
			}
			BlockChain.getInstance().getUTXOs().remove(i.UTXO.id);
		}
		return true;
	}

	/**
	 * 生成签名
	 */
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		signature = StringUtil.applyECDSASig(privateKey,data);		
	}

	/**
	 * 验证我们签署的数据没有被篡改
	 * @return
	 */
	public boolean verifySignature() {
		//data: 发送人的公钥+收款人的公钥+发送的金额
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		return StringUtil.verifyECDSASig(sender, data, signature);
	}


	/**
	 * 从数据中解析出发送的金额
	 */
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) {
                continue; //如果事务不能被发现跳过它，这种行为可能不是最佳的
            }
			total += i.UTXO.value;
		}
		return total;
	}
	
	/**
	 * 从数据中解析出
	 */
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}

	public String calulateHash() {
		sequence++; //增加序列以避免两个相同的事务具有相同的散列。
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(reciepient) +
				Float.toString(value) + sequence);
	}
}
