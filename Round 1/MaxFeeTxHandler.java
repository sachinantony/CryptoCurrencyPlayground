import java.util.*;

public class MaxFeeTxHandler {

    UTXOPool uPool;

    public MaxFeeTxHandler(UTXOPool utxoPool) {
        uPool = utxoPool;
    }

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
            if (claimedSet.contains(u)) {
                return false;
            } else {
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
        for (int j = 0; j < txOpList.size(); j++) {
            if (txOpList.get(j).value < 0) return false;
            totalOPValue = totalOPValue + txOpList.get(j).value;
        }

        if (totalIPValue >= totalOPValue) {
            return true;
        } else {
            return false;
        }
    }


    public double calculateFees(Transaction tx) {
        ArrayList<Transaction.Input> txIpList = tx.getInputs();
        double ipVal = 0.0;
        double opVal = 0.0;
        double diffVal;
        for (int i = 0; i < txIpList.size(); i++) {
            Transaction.Input tIn = txIpList.get(i);
            UTXO u = new UTXO(tIn.prevTxHash, tIn.outputIndex);
            if (!isValidTx(tx) || !uPool.contains(u)) continue;
            Transaction.Output tOp = uPool.getTxOutput(u);
                ipVal = ipVal + tOp.value;
        }

        ArrayList<Transaction.Output> txOpList = tx.getOutputs();
        for (int j = 0; j < txOpList.size(); j++) {
            opVal = opVal + txOpList.get(j).value;

        }
        diffVal = ipVal - opVal;
        return diffVal;
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> sortedTx = new ArrayList<Transaction>(Arrays.asList(possibleTxs));
        Collections.sort(sortedTx, new Comparator<Transaction>() {
            public int compare(Transaction tx1, Transaction tx2) {
                return Double.valueOf(calculateFees(tx2)).compareTo(calculateFees(tx1));
            }
        });

        ArrayList<Transaction> acceptedTransaction = new ArrayList<Transaction>();
        ArrayList<Transaction> rejectedTransaction = new ArrayList<Transaction>();

        for (Transaction pTx : sortedTx) {
            if (isValidTx(pTx)) {
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