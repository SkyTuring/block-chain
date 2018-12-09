package com.fuyq.qkl;

import com.fuyq.util.StringUtil;
import org.junit.Test;

import java.security.Security;

/**
 *
 */
public class BlockChainTest {
    @Test
    public void createChain(){
        BlockChain bc = BlockChain.getInstance();

        System.out.println("正在创建第一个区块链....... ");
        bc.addBlock(new Block("0"));

        System.out.println("正在创建第二个区块链....... ");
        bc.addBlock(new Block(bc.getLastHash()));

        System.out.println("正在创建第三个区块链.......");
        bc.addBlock(new Block(bc.getLastHash()));

        String bcJson = StringUtil.getJson(bc);
        System.out.println(bcJson);
        //注意，这里因为只是创建区块并没有在区块中添加任何一个事务
        //所以genesisTransaction.outputs.get(0).id 会为空指针
        //isChainValid();
    }

    @Test
    public void transaction(){

        BlockChain bc = BlockChain.getInstance();

        //将我们的block添加到区块链ArrayList:
        //设置Bouncey作为安全提供程序
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //创建钱包
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        Wallet coinBase = new Wallet();

        //创建创世交易（第一笔交易），向walletA发送50个星币:
        bc.setGenesisTransaction(new Transaction(coinBase.publicKey, walletA.publicKey, 50f, null));
        Transaction tc = bc.getGenesisTransaction();
        //手动签署《创世纪》交易。
        tc.generateSignature(coinBase.privateKey);
        //手动设置交易id。
        tc.transactionId = tc.calulateHash();
        //手动添加交易输出。
        tc.outputs.add(
                new TransactionOutput(tc.reciepient,
                        tc.value,
                        tc.transactionId));
        //在UTXOs列表中存储第一个事务，非常重要！
        bc.getUTXOs().put(
                tc.outputs.get(0).id,
                tc.outputs.get(0));

        System.out.println("#info:创造和开采创世纪块...... ");
        // 创世区块
        Block genesis = new Block("0");
        // 把事务放到区块里
        genesis.addTransaction(tc);
        // 把区块放大区块链中
        bc.addBlock(genesis);

        //测试
        Block block1 = new Block(genesis.hash);
        System.out.println("钱包A的 余额为: " + walletA.getBalance());
        System.out.println("WalletA正试图向WalletB发送资金(40)……");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        bc.addBlock(block1);
        System.out.println("钱包A的 余额为:" + walletA.getBalance());
        System.out.println("钱包B的 余额为: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("WalletA尝试发送更多的资金(1000)……");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        bc.addBlock(block2);
        System.out.println("钱包A的 余额为: " + walletA.getBalance());
        System.out.println("钱包B的 余额为: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("WalletB正试图向WalletA发送资金(20)……");
        block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
        System.out.println("钱包A的 余额为:  " + walletA.getBalance());
        System.out.println("钱包B的 余额为:  " + walletB.getBalance());

        System.out.println(StringUtil.getJson(bc.getBlocks()));
        System.out.println(walletA);
        System.out.println(walletB);

        //验证合法性
        bc.isChainValid();

    }
}
