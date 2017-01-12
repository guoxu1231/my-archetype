package dominus.intg.ethereum;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.springframework.util.StopWatch;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import rx.Subscription;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertNotNull;

public class TestWeb3j extends DominusJUnit4TestBase {
    protected Web3j web3;
    protected Credentials credentials;
    protected BigInteger gasPrice;
    protected BigInteger gasLimit = BigInteger.valueOf(300000);//TODO bug in web3j?4712388


    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        web3 = Web3j.build(new HttpService(properties.getProperty("geth.address")));
        credentials = WalletUtils.loadCredentials(properties.getProperty("geth.coinbase.password"), properties.getProperty("geth.coinbase.wallet.file"));
        gasPrice = web3.ethGasPrice().send().getGasPrice();
        logger.info("current price per gas in wei {}", gasPrice);
        logger.info("transaction nonce:{}", web3.ethGetTransactionCount(properties.getProperty("geth.coinbase.address"), DefaultBlockParameterName.LATEST).send().getTransactionCount());
    }

    @Test
    public void testPrintBasicInfo() throws ExecutionException, InterruptedException, IOException {
        out.printf("geth node(%s)\n", properties.getProperty("geth.address"));
        out.printf("\tpeer count:%s\n", web3.netPeerCount().send().getQuantity());
        out.printf("\tsync status::%s\n", web3.ethSyncing().send().isSyncing());
        out.printf("\tcoinbase address:%s\n", web3.ethCoinbase().send().getAddress());
        out.printf("\tcoinbase account balance:%s\n", web3.ethGetBalance(properties.getProperty("geth.coinbase.address"), DefaultBlockParameter.valueOf("latest")));
        out.printf("\tmining status:%s\n", web3.ethMining().send().isMining());
        out.printf("\tblock number:%s\n", web3.ethBlockNumber().send().getBlockNumber());
        out.printf("\tnonce:%s\n", web3.ethGetTransactionCount(properties.getProperty("geth.coinbase.address"), DefaultBlockParameterName.LATEST).send().getTransactionCount());
    }

    @Test
    public void testObserveNewTransaction() {
        Subscription subscription = web3.transactionObservable().subscribe(tx -> {
            logger.info("new transaction {}", tx.getBlockNumber());
        });
        sleep(50000);
    }


    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }

    @Test
    public void testDeployContract() throws ExecutionException, InterruptedException {
        Future<Marriage> future = Marriage.deploy(web3, credentials, gasPrice, gasLimit, BigInteger.valueOf(0));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("deploying contract...");
        Marriage contract = future.get();
        stopWatch.stop();
        logger.info(stopWatch.toString());
        logger.info("contract address {}", contract.getContractAddress());
        assertNotNull(contract.getContractAddress());
        TransactionReceipt receipt = contract.createMarriage(new Bytes32(Arrays.copyOf("shawguo".getBytes(), 32)), new Bytes32(Arrays.copyOf("shawguo".getBytes(), 32)), new Uint256(BigInteger.valueOf(System.currentTimeMillis())),
                new Bytes32(Arrays.copyOf("married".getBytes(), 32)), new Bytes32(Arrays.copyOf("hello world, we are married!".getBytes(), 32))).get();//TODO web3j bug? inconvenient
        logger.info(ToStringBuilder.reflectionToString(receipt));
    }

    //0xed4d2eb2e1ba761ae8147b35c3a9fb797acd832a
    @Test
    public void testLocalCall() throws ExecutionException, InterruptedException {
        Marriage contract = Marriage.load("0xed4d2eb2e1ba761ae8147b35c3a9fb797acd832a", web3, credentials, gasPrice, gasLimit);
        //local call
        logger.info(new String(contract.partner1().get().getValue()));
        logger.info(new String(contract.partner2().get().getValue()));
        logger.info(new String(contract.marriageStatus().get().getValue()));
        String uniqueStr = "happy new year "+System.currentTimeMillis();
        TransactionReceipt receipt = contract.setStatus(new Bytes32(Arrays.copyOf(uniqueStr.getBytes(), 32))).get();
        logger.info(ToStringBuilder.reflectionToString(receipt));
        logger.info(new String(contract.marriageStatus().get().getValue()));
    }

    //send multiple transaction at the same time without waiting for receipt.
    @Test
    public void testSendMultipleTransaction() throws ExecutionException, InterruptedException {
        Marriage contract = Marriage.load("0xed4d2eb2e1ba761ae8147b35c3a9fb797acd832a", web3, credentials, gasPrice, gasLimit);
        Future<TransactionReceipt>[] futures = new Future[100];

        for (int i = 0; i < 2; i++) {
            String status = String.format("married %dth anniversary", i);
            futures[i] = contract.setStatus(new Bytes32(Arrays.copyOf(status.getBytes(), 32)));
            logger.info("{} tx-change status to {}", i, status);

//            sleep(14000);
        }
        for (int i = 0; i < 2; i++) {
            TransactionReceipt receipt = futures[i].get();
            logger.info(ToStringBuilder.reflectionToString(receipt));//TODO failed to send multiple transaction at same time
        }
    }

    @Test
    public void testDuplicateTransaction() {
        //same content
    }
}
