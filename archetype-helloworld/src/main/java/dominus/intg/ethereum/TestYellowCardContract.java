package dominus.intg.ethereum;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;
import org.springframework.util.StopWatch;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertNotNull;

public class TestYellowCardContract extends TestWeb3j {

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
    }

    @Test
    public void testDeployContract() throws ExecutionException, InterruptedException {
        Future<YellowCard> future = YellowCard.deploy(web3, credentials, gasPrice, gasLimit, BigInteger.valueOf(0));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("deploying contract...");
        YellowCard contract = future.get();
        stopWatch.stop();
        logger.info(stopWatch.toString());
        logger.info("contract address {}", contract.getContractAddress());
        assertNotNull(contract.getContractAddress());
//        TransactionReceipt receipt = contract.createMarriage(new Bytes32(Arrays.copyOf("shawguo".getBytes(), 32)), new Bytes32(Arrays.copyOf("shawguo".getBytes(), 32)), new Uint256(BigInteger.valueOf(System.currentTimeMillis())),
//                new Bytes32(Arrays.copyOf("married".getBytes(), 32)), new Bytes32(Arrays.copyOf("hello world, we are married!".getBytes(), 32))).get();//TODO web3j bug? inconvenient
//        logger.info(ToStringBuilder.reflectionToString(receipt));
    }

    @Test
    public void testCreateCard() throws ExecutionException, InterruptedException {
        YellowCard contract = YellowCard.load("0x7cc749a3cebc7a008eb0daf7940b268b40d3dc05", web3, credentials, gasPrice, gasLimit);
        TransactionReceipt receipt = contract.Create(new Int256(BigInteger.valueOf(10086))).get();

    }
}
