package com.fuyq.qkl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 创建区块链
 */
public class BlockChain {
    /**
     * 存放所有的区块集合
     */
    private List<Block> blocks = new ArrayList<>();
    /**
     * 未使用的交易作为可用的输入
     */
    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    /**
     * 挖矿的难度，数字越大越难
     */
    public static int difficulty = 3;
    /**
     * 最低的交易金额
     */
    public static float minimumTransaction = 0.1f;
    /**
     * 交易信息
     */
    private Transaction genesisTransaction;

    /**
     * 交易信息
     */
    public ArrayList<Transaction> upgTcanscction;

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public HashMap<String, TransactionOutput> getUTXOs() {
        return UTXOs;
    }

    public void setUTXOs(HashMap<String, TransactionOutput> UTXOs) {
        this.UTXOs = UTXOs;
    }

    public Transaction getGenesisTransaction() {
        return genesisTransaction;
    }

    public void setGenesisTransaction(Transaction genesisTransaction) {
        this.genesisTransaction = genesisTransaction;
    }

    private static BlockChain blockChain;
    private BlockChain() {

    }
    public static BlockChain getInstance(){
        if (blockChain==null){
            blockChain = new BlockChain();
            return blockChain;
        }else {
            return blockChain;
        }
    }

    /**
     * 检查区块链的完整性
     */
    public Boolean isChainValid() {
        Block currentBlock = null;
        Block previousBlock = null;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        //给定块状态下未使用事务的临时工作列表。
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //循环区块链检查散列:
        for (int i = 1; i < blocks.size(); i++) {
            currentBlock = blocks.get(i);
            previousBlock = blocks.get(i - 1);
            //比较注册散列和计算散列:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("当前的hash与预期的不相等");
                return false;
            }
            //比较以前的散列和注册的先前的散列
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("当前的previousHash不等于上一个区块的hash");
                return false;
            }
            //检查哈希是否被使用
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("这个区块还没有被开采。。。");
                return false;
            }

        }

        //检查事务的合法性
        TransactionOutput tempOutput;
        for (int t = 0; t < currentBlock.transactions.size(); t++) {
            Transaction currentTransaction = currentBlock.transactions.get(t);

            if (!currentTransaction.verifySignature()) {
                System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                return false;
            }
            if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                return false;
            }

            for (TransactionInput input : currentTransaction.inputs) {
                tempOutput = tempUTXOs.get(input.transactionOutputId);

                if (tempOutput == null) {
                    System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                    return false;
                }

                if (input.UTXO.value != tempOutput.value) {
                    System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                    return false;
                }

                tempUTXOs.remove(input.transactionOutputId);
            }

            for (TransactionOutput output : currentTransaction.outputs) {
                tempUTXOs.put(output.id, output);
            }

            if (currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                return false;
            }
            if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                return false;
            }

        }
        return true;
    }

    /**
     * 增加一个新的区块
     * 并将改去放放到区块链中
     */
    public void addBlock(Block newBlock) {
        //将newBlock建造成一个区块
        newBlock.mineBlock(difficulty);
        //将newBlock 放到区块链中
        blocks.add(newBlock);
    }

    public String getLastHash(){
        return blocks.get(blocks.size()-1).hash;
    }
}
