package com.fuyq.app;

import com.fuyq.qkl.*;
import com.fuyq.util.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Security;

@Controller
@RequestMapping("/qkl")
public class MapperController {

    private static BlockChain bc;
    private static Block upgBlock;
    private static Wallet w1;
    private static Wallet w2;
    private static Wallet coinBase;
    static {
        bc = BlockChain.getInstance();
        bc.addBlock(new Block("0"));
        //设置Bouncey作为安全提供程序
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        w1 = new Wallet("Tom钱包1");
        w2 = new Wallet("Jerry钱包2");
        coinBase = new Wallet("coinBase");
        upgBlock = new Block(bc.getLastHash());
    }

    @ResponseBody
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public String info() {
        return "qkl";
    }

    @ResponseBody
    @RequestMapping(value = "/mine", method = RequestMethod.GET)
    public String mine() {
        bc.addBlock(upgBlock);
        upgBlock = new Block(bc.getLastHash());
        return StringUtil.getJson(bc);
    }

    @ResponseBody
    @RequestMapping(value = "/chain", method = RequestMethod.GET)
    public String chain() {
        return StringUtil.getJson(bc);
    }

    @ResponseBody
    @RequestMapping(value = "/w", method = RequestMethod.GET)
    public String w() {
        return w1.toString()+"\n"+w2.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/trans", method = RequestMethod.POST)
    public String trans(String trans) {
        String[] strings = trans.split(",");
        float val = Float.parseFloat(strings[2]);
        if (strings[0].equals("1")){
            upgBlock.addTransaction(w1.sendFunds(w2.publicKey,val));
            return "钱包1转给钱包2  "+val+"\n\n"+w1.toString()+"\n"+w2.toString();
        }else {
            upgBlock.addTransaction(w2.sendFunds(w1.publicKey,val));
            return "钱包2转给钱包1  "+val+"\n\n"+w1.toString()+"\n"+w2.toString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/mine1", method = RequestMethod.GET)
    public String mine1() {
        bc.setGenesisTransaction(new Transaction(coinBase.publicKey, w1.publicKey, 50f, null));
        Transaction tc = bc.getGenesisTransaction();
        tc.generateSignature(coinBase.privateKey);
        tc.transactionId = "0";
        tc.outputs.add(new TransactionOutput(tc.reciepient, tc.value, tc.transactionId));
        bc.getUTXOs().put(tc.outputs.get(0).id, tc.outputs.get(0));
        // 创世区块
        upgBlock.isFaxing = true;
        upgBlock.addTransaction(tc);
        bc.addBlock(upgBlock);
        upgBlock = new Block(bc.getLastHash());
        return w1.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/mine2", method = RequestMethod.GET)
    public String mine2() {
        bc.setGenesisTransaction(
                new Transaction(coinBase.publicKey, w2.publicKey, 50f, null));
        Transaction tc = bc.getGenesisTransaction();
        tc.generateSignature(coinBase.privateKey);
        tc.transactionId = "0";
        tc.outputs.add(
                new TransactionOutput(tc.reciepient, tc.value, tc.transactionId));
        BlockChain.getInstance().getUTXOs().put(tc.outputs.get(0).id, tc.outputs.get(0));
        upgBlock.isFaxing = true;
        upgBlock.addTransaction(tc);
        bc.addBlock(upgBlock);
        upgBlock = new Block(bc.getLastHash());
        return w2.toString();
    }
}
