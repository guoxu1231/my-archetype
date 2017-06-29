package dominus.intg.stellar;


import org.junit.Test;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.AccountResponse;
import origin.common.junit.DominusJUnit4TestBase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class TestStellarClient extends DominusJUnit4TestBase {

    @Test
    public void testCreateAccount() throws IOException {
        KeyPair pair = KeyPair.random();
        System.out.println(new String(pair.getSecretSeed()));
// SAV76USXIJOBMEQXPANUOQM6F5LIOTLPDIDVRJBFFE2MDJXG24TAPUU7
        System.out.println(pair.getAccountId());
// GCFXHS4GXL6BVUCXBWXGTITROWLVYXQKQLF4YH5O5JT3YZXCYPAFBJZB

        //EE: get initial balance of 20 lumens from friendbot
        String friendbotUrl = String.format("https://horizon-testnet.stellar.org/friendbot?addr=%s", pair.getAccountId());
        InputStream response = new URL(friendbotUrl).openStream();
        String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
        System.out.println("SUCCESS! You have a new account :)\n" + body);

        //EE: query balance
        Server server = new Server("https://horizon-testnet.stellar.org");
        AccountResponse account = server.accounts().account(pair);
        System.out.println("Balances for account " + pair.getAccountId());
        for (AccountResponse.Balance balance : account.getBalances()) {
            System.out.println(String.format(
                    "Type: %s, Code: %s, Balance: %s",
                    balance.getAssetType(),
                    balance.getAssetCode(),
                    balance.getBalance()));
            if ("native".equalsIgnoreCase(balance.getAssetType()))
                assertEquals("10000.0000000", balance.getBalance());
        }
    }


}
