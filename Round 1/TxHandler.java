

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class TxHandler {

UTXOPool uPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.

     */
    public TxHandler(UTXOPool utxoPool) {
        uPool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.

     */
    public boolean isValidTx(Transaction tx) {
        ArrayList<Transaction.Input> txIpList = tx.getInputs();
        ArrayList<Transaction.Output> txOpList = tx.getOutputs();
        HashSet<UTXO> claimedSet = new HashSet<UTXO>();
        double totalIPValue = 0.0;
        for (int i = 0; i < txIpList.size(); i++) {
            Transaction.Input tIn = txIpList.get(i);
            UTXO u = new UTXO(tIn.prevTxHash, tIn.outputIndex);
            if (!uPool.contains(u)) {
                return false;
            }
            if(claimedSet.contains(u)) {
            	return false;
            }
            else {
            	claimedSet.add(u);
            }
            Transaction.Output tOp = uPool.getTxOutput(u);
            if (tOp == null) {
                return false;
            }

            RSAKey address = tOp.address;
            byte[] msg = tx.getRawDataToSign(i);
            byte[] sign = tIn.signature;

            if (!address.verifySignature(msg, sign)) {
                return false;
            }

            totalIPValue = totalIPValue + tOp.value;
        }

        double totalOPValue = 0.0;
        for(int j=0; j<txOpList.size();j++)
        {
            if (txOpList.get(j).value < 0) return false;
            totalOPValue = totalOPValue + txOpList.get(j).value;
        }

        if (totalIPValue >= totalOPValue) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> possibleTxList = new ArrayList<Transaction>(Arrays.asList(possibleTxs));
        ArrayList<Transaction> acceptedTransaction = new ArrayList<Transaction>();
        ArrayList<Transaction> rejectedTransaction = new ArrayList<Transaction>();
        for(Transaction pTx : possibleTxList)
        {
            if(isValidTx(pTx)){
                acceptedTransaction.add(pTx);
                for (Transaction.Input tIn : pTx.getInputs()) {
                    UTXO utxoToRemove = new UTXO(tIn.prevTxHash, tIn.outputIndex);
                    uPool.removeUTXO(utxoToRemove);
                }
                int i = 0;
                for (Transaction.Output tOp : pTx.getOutputs()) {
                    UTXO utxoToAdd = new UTXO(pTx.getHash(), i);
                    uPool.addUTXO(utxoToAdd, tOp);
                    i++;
                }

            }
            else
            {
                rejectedTransaction.add(pTx);
            }
        }
        int accArrSize = acceptedTransaction.size();
        return acceptedTransaction.toArray(new Transaction[accArrSize]);
    }

}
