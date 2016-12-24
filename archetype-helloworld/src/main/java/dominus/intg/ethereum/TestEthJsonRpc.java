package dominus.intg.ethereum;


import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * Required:
 * --rpcaddr "0.0.0.0" expose rpc to anyone
 * geth --unlock 0 --rpc --rpcport "8545" --ipcapi "admin,db,eth,debug,miner,net,shh,txpool,personal,web3" --rpcapi "db,eth,net,web3" --ipcpath /home/deploy/.ethereum-ipc --rpcaddr "0.0.0.0"
 */
public class TestEthJsonRpc extends DominusJUnit4TestBase {

    JsonRpcHttpClient client;
    static final String HEX_PREFIX = "0x";

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        out.print(ANSI_PURPLE);
        client = new JsonRpcHttpClient(new URL(properties.getProperty("geth.address")));
        out.printf("geth node(%s) status\n", properties.getProperty("geth.address"));
        try {
            out.printf("\tpeer count:%s\n", Integer.decode(client.invoke("net_peerCount", null, String.class)));
            out.printf("\tsync status:%s\n", client.invoke("eth_syncing", null, String.class));
            //Set your coinbase before you start mining else you will not get your block reward!
            out.printf("\tcoinbase address:%s\n", client.invoke("eth_coinbase", null, String.class));
            //integer of the current balance in wei.
            out.printf("\tcoinbase account balance:%s\n", (client.invoke("eth_getBalance", new String[]{properties.getProperty("geth.coinbase.address"), "latest"}, String.class)));
            out.printf("\tmining status:%s\n", client.invoke("eth_mining", null, String.class));
//            out.printf("\teth accounts:%s\n", client.invoke("eth_accounts", null, String.class));TODO
            out.printf("\tblock number:%s\n", Integer.decode(client.invoke("eth_blockNumber", null, String.class)));
//            out.printf("\tavailable compilers:%s\n", client.invoke("eth_getCompilers", null, String.class));
//            out.printf("\twhisper protocol version:%s\n", client.invoke("shh_version", null, String.class));

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        out.print(ANSI_RESET);
    }

    @Test
    public void test_eth_getTransactionCount() throws Throwable {
        out.println(Integer.decode(client.invoke("eth_getTransactionCount",
                new String[]{"0x65af310ec47990ac2ecf69b6adea6f213bcce42e", "latest"}, String.class)));
    }

    @Test
    public void testDeployContract() throws Throwable {
        Map<String, Object> params = new HashMap<>();
        params.put("from", properties.getProperty("geth.coinbase.address"));
        params.put("gas", 3000000);
        params.put("data", properties.getProperty("solidity.marriage.bytecode"));

        String transactionHash = client.invoke("eth_sendTransaction", new Object[]{params}, String.class);
        StopWatch stopWatch = new StopWatch();
        logger.info("contract is deployed, tx-hash is {}", transactionHash);
        stopWatch.start();
        Map receiptMap = null;
        while (receiptMap == null) {
            receiptMap = client.invoke("eth_getTransactionReceipt", new Object[]{transactionHash}, Map.class);
            sleep(2000);
        }
        //about 14s to be mined.
        stopWatch.stop();
        logger.info(stopWatch.toString());
        logger.info("transaction receipt is {}",receiptMap.toString());
    }

    @Test
    public void testInvokeContract() {

    }


}
