package dominus.intg.ethereum;

import java.lang.String;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Future;
import org.web3j.abi.Contract;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * <p>Auto generated code.<br>
 * <strong>Do not modifiy!</strong><br>
 * Please use {@link org.web3j.codegen.SolidityFunctionWrapperGenerator} to update.</p>
 */
public final class Marriage extends Contract {
    private static final String BINARY = "606060405234610000575b33600060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b5b610a738061005c6000396000f300606060405236156100c3576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680631158f696146100d5578063215a3bcc1461012657806326826bf8146101475780634d7d8a601461019e57806351605d80146101c1578063893d20e8146102575780638b35a244146102a65780638da5cb5b146102d1578063a4c7c7b314610320578063ac5ce03b1461034b578063d2521034146103e1578063e771066f1461040c578063eafb704f14610463575b34610000576100d35b610000565b565b005b346100005761012460048080356000191690602001909190803560001916906020019091908035906020019091908035600019169060200190919080356000191690602001909190505061049a565b005b3461000057610145600480803560001916906020019091905050610553565b005b346100005761019c600480803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919050506105e5565b005b34610000576101ab61072d565b6040518082815260200191505060405180910390f35b34610000576101ce610733565b604051808060200182810382528381815181526020019150805190602001908083836000831461021d575b80518252602083111561021d576020820191506020810190506020830392506101f9565b505050905090810190601f1680156102495780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576102646107d1565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34610000576102b36107da565b60405180826000191660001916815260200191505060405180910390f35b34610000576102de6107e0565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b346100005761032d610806565b60405180826000191660001916815260200191505060405180910390f35b346100005761035861080c565b60405180806020018281038252838181518152602001915080519060200190808383600083146103a7575b8051825260208311156103a757602082019150602081019050602083039250610383565b505050905090810190601f1680156103d35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576103ee6108aa565b60405180826000191660001916815260200191505060405180910390f35b3461000057610461600480803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919050506108b0565b005b3461000057610498600480803590602001909190803560001916906020019091908035600019169060200190919050506109f8565b005b6000600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141561054a57856001816000191690555084600281600019169055508360038190555061051883610553565b7f4d6172726961676520436f6e7472616374204372656174696f6e000000000000905061054860035482846109f8565b5b5b5b505050505050565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614156105e15780600481600019169055506105df427f4368616e67656420537461747573000000000000000000000000000000000000836109f8565b5b5b5b50565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415610729578060059080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061068757805160ff19168380011785556106b5565b828001600101855582156106b5579182015b828111156106b4578251825591602001919060010190610699565b5b5090506106da91905b808211156106d65760008160009055506001016106be565b5090565b5050610727427f456e7465726564204d6172726961676520496d616765000000000000000000007f496d61676520697320696e2049504653000000000000000000000000000000006109f8565b5b5b5b50565b60035481565b60058054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156107c95780601f1061079e576101008083540402835291602001916107c9565b820191906000526020600020905b8154815290600101906020018083116107ac57829003601f168201915b505050505081565b60008090505b90565b60025481565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60015481565b60068054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108a25780601f10610877576101008083540402835291602001916108a2565b820191906000526020600020905b81548152906001019060200180831161088557829003601f168201915b505050505081565b60045481565b600060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614156109f4578060069080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061095257805160ff1916838001178555610980565b82800160010185558215610980579182015b8281111561097f578251825591602001919060010190610964565b5b5090506109a591905b808211156109a1576000816000905550600101610989565b5090565b50506109f2427f456e7465726564204d617272696167652050726f6f66000000000000000000007f4d617272696167652070726f6f6620696e2049504653000000000000000000006109f8565b5b5b5b50565b806000191682600019167feb3cfa47411819a5b9272fdf6475da3a4b255abaae92d9815ba5985cdd1b8b244286604051808381526020018281526020019250505060405180910390a35b5050505600a165627a7a72305820e267529d66a474590e59e3632c715d02592a2de8cb2ce5881cee0323e16914ba0029";

    private Marriage(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public Future<TransactionReceipt> createMarriage(Bytes32 partner1Entry, Bytes32 partner2Entry, Uint256 marriageDateEntry, Bytes32 statusEntry, Bytes32 descriptionEntry) {
        Function function = new Function("createMarriage", Arrays.<Type>asList(partner1Entry, partner2Entry, marriageDateEntry, statusEntry, descriptionEntry), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> setStatus(Bytes32 status) {
        Function function = new Function("setStatus", Arrays.<Type>asList(status), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> setImage(DynamicBytes IPFSImageHash) {
        Function function = new Function("setImage", Arrays.<Type>asList(IPFSImageHash), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> marriageDate() {
        Function function = new Function("marriageDate", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<DynamicBytes> imageHash() {
        Function function = new Function("imageHash", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> getOwner() {
        Function function = new Function("getOwner", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Bytes32> partner2() {
        Function function = new Function("partner2", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Address> owner() {
        Function function = new Function("owner", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Bytes32> partner1() {
        Function function = new Function("partner1", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<DynamicBytes> marriageProofDoc() {
        Function function = new Function("marriageProofDoc", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Bytes32> marriageStatus() {
        Function function = new Function("marriageStatus", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> marriageProof(DynamicBytes IPFSProofHash) {
        Function function = new Function("marriageProof", Arrays.<Type>asList(IPFSProofHash), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> majorEventFunc(Uint256 eventTimeStamp, Bytes32 name, Bytes32 description) {
        Function function = new Function("majorEventFunc", Arrays.<Type>asList(eventTimeStamp, name, description), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public static Future<Marriage> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialValue) {
        return deployAsync(Marriage.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialValue);
    }

    public EventValues processMajorEventEvent(TransactionReceipt transactionReceipt) {
        Event event = new Event("MajorEvent", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return extractEventParameters(event, transactionReceipt);
    }

    public static Marriage load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Marriage(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }
}
