package dominus.intg.stellar;


import org.junit.Test;
import org.stellar.sdk.*;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;
import origin.common.junit.DominusJUnit4TestBase;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestStellarClient extends DominusJUnit4TestBase {

    KeyPair source;
    KeyPair destination;
    Server server = new Server("https://horizon-testnet.stellar.org");
    private static final String NATIVE_ASSET_CODE = "native";

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        Network.useTestNetwork();

        source = generateAccount();
        destination = generateAccount();

        BigDecimal balance = nativeBalance(source, NATIVE_ASSET_CODE);
        assertEquals("10000.0000000", balance.toPlainString());
    }

    @Test
    public void sendAndReceiveMoney() throws IOException {
        doTransfer(source, destination, "10");
        //EE: Finally, every transaction costs a small base fee. 100 stroops per operation (that’s 0.00001 XLM)
        assertEquals("9989.9999900", nativeBalance(source, NATIVE_ASSET_CODE).toPlainString());
        assertEquals("10010.0000000", nativeBalance(destination, NATIVE_ASSET_CODE).toPlainString());
    }

    @Test
    public void receivePayments() throws IOException, InterruptedException {

        new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    doTransfer(source, destination, "10");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Create an API call to query payments involving the account.
        PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(source);

        // `stream` will send each recorded payment, one by one, then keep the
        // connection open and continue to send you new payments as they occur.
        CountDownLatch latch = new CountDownLatch(20);
        paymentsRequest.stream(new EventListener<OperationResponse>() {
            @Override
            public void onEvent(OperationResponse payment) {
                // TODO Record the paging token so we can start from here next time.
                // savePagingToken(payment.getPagingToken());

                // The payments stream includes both sent and received payments. We only
                // want to process received payments here.
                if (payment instanceof PaymentOperationResponse) {
                    if (((PaymentOperationResponse) payment).getTo().equals(source)) {
                        return;
                    }

                    String amount = ((PaymentOperationResponse) payment).getAmount();
                    Asset asset = ((PaymentOperationResponse) payment).getAsset();
                    String assetName;
                    if (asset.equals(new AssetTypeNative())) {
                        assetName = "lumens";
                    } else {
                        StringBuilder assetNameBuilder = new StringBuilder();
                        assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getCode());
                        assetNameBuilder.append(":");
                        assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getIssuer().getAccountId());
                        assetName = assetNameBuilder.toString();
                    }

                    StringBuilder output = new StringBuilder();
                    output.append(amount);
                    output.append(" ");
                    output.append(assetName);
                    output.append(" from ");
                    output.append(((PaymentOperationResponse) payment).getFrom().getAccountId());
                    System.out.println(output.toString());
                    latch.countDown();
                }

            }
        });
        assertTrue(latch.await(2, TimeUnit.MINUTES));
    }


    private BigDecimal nativeBalance(KeyPair account, String assetCode) throws IOException {
        AccountResponse accountResponse = server.accounts().account(account);
        System.out.print("Balances for account " + account.getAccountId());
        for (AccountResponse.Balance balance : accountResponse.getBalances()) {
            System.out.println(String.format(
                    " \t[Type: %s, Code: %s, Balance: %s]",
                    balance.getAssetType(),
                    balance.getAssetCode(),
                    balance.getBalance()));
            if (assetCode.equalsIgnoreCase(balance.getAssetType()))
                return new BigDecimal(balance.getBalance());
        }
        throw new RuntimeException(account.getAccountId() + " native balance is null!");
    }

    //EE: generate keypair and give initial balance of 20 lumens from friendbot
    private KeyPair generateAccount() throws IOException {
        KeyPair pair = KeyPair.random();
        System.out.println(new String(pair.getSecretSeed()));
        // SAV76USXIJOBMEQXPANUOQM6F5LIOTLPDIDVRJBFFE2MDJXG24TAPUU7
        System.out.println(pair.getAccountId());
        // GCFXHS4GXL6BVUCXBWXGTITROWLVYXQKQLF4YH5O5JT3YZXCYPAFBJZB
        String friendbotUrl = String.format("https://horizon-testnet.stellar.org/friendbot?addr=%s", pair.getAccountId());
        InputStream response = new URL(friendbotUrl).openStream();
        String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
        System.out.println("SUCCESS! You have a new account :)\n" + body);

        return pair;
    }

    private boolean doTransfer(KeyPair source, KeyPair destination, String acount) throws IOException {
        // First, check to make sure that the destination account exists.
        // You could skip this, but if the account does not exist, you will be charged
        // the transaction fee when the transaction fails.
        // It will throw HttpResponseException if account does not exist or there was another error.
        server.accounts().account(destination);
        // If there was no error, load up-to-date information on your account.This number starts equal to the ledger number at which the account was created.
        AccountResponse sourceAccount = server.accounts().account(source);

        // Start building the transaction.
        Transaction transaction = new Transaction.Builder(sourceAccount)
                .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), acount).build())
                // A memo allows you to add your own metadata to a transaction. It's
                // optional and does not affect how Stellar treats the transaction.
                .addMemo(Memo.text("Test Transaction"))
                .build();
        // Sign the transaction to prove you are actually the person sending it.
        transaction.sign(source);
//EE: Finally, every transaction costs a small base fee. 100 stroops per operation (that’s 0.00001 XLM)
        // And finally, send it off to Stellar!
        try {
            SubmitTransactionResponse response = server.submitTransaction(transaction);
            System.out.println(String.format("transfer %s from %s to %s success!", acount, source.getAccountId(), destination.getAccountId()));
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            System.out.println(e.getMessage());
        }
        return true;
    }

}
