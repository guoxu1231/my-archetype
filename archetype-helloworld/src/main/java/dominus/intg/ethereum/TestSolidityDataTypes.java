package dominus.intg.ethereum;


import dominus.intg.ethereum.contract.DataTypeTest;
import org.junit.Test;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestSolidityDataTypes extends TestWeb3j {

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        //TODO deploy contract
        if (!StringUtils.hasText(contractAddress))
            contractAddress = this.deployContract();
    }

    private String deployContract() throws ExecutionException, InterruptedException {
        Future<DataTypeTest> future = DataTypeTest.deploy(web3, credentials, gasPrice, gasLimit, BigInteger.valueOf(0));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("deploying contract...");
        DataTypeTest contract = future.get();
        stopWatch.stop();
        logger.info(stopWatch.toString());
        logger.info("contract address {}", contract.getContractAddress());
        assertNotNull(contract.getContractAddress());
        return contract.getContractAddress();
    }

//    static String contractAddress;
    static String contractAddress = "0x3cd76723e9bb167948d22f56c26d00e26ae17d6d";

    //address=0x4a6c414f403fde2b543d72c9fcff1f4ad34d90cc
    @Test
    public void testBytesN() throws ExecutionException, InterruptedException {
        DataTypeTest contract = DataTypeTest.load(contractAddress, web3, credentials, gasPrice, gasLimit);
        TransactionReceipt receipt = contract.setBytesN(new Bytes16("1234567890123456".getBytes())).get();
        logger.info("setBytesN success {}", receipt.getBlockNumber());
        logger.info(String.valueOf(contract.getBytesN().get().getValue())); //TODO default Accessor Functions error
    }

    @Test
    public void testUsingLibrary() throws ExecutionException, InterruptedException {
        DataTypeTest contract = DataTypeTest.load(contractAddress, web3, credentials, gasPrice, gasLimit);
        assertEquals(4, contract.testUsinglibrary().get().getValue().intValue());
    }

}
