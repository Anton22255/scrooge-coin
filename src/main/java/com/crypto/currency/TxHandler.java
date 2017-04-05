package com.crypto.currency;

import java.util.ArrayList;
import java.util.List;

import com.crypto.currency.Transaction.Input;
import com.crypto.currency.Transaction.Output;

public class TxHandler {

	private UTXOPool currentPool;

	/**
	 * Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is {@code utxoPool}. This should make a copy of
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		this.currentPool = new UTXOPool(utxoPool);
	}

	public interface TxValidator {

		boolean isTransactionNotInPool(UTXO currentUnSpentTransaction);

		boolean isSignatureInValid(Transaction tx, Input transactionInputEntry);

		boolean isTransactionDuplicated(List<UTXO> allTransactions, UTXO currentUnSpentTransaction);

		boolean isValueInvalid(Transaction tx, Input transactionInputEntry);

		boolean isTransactionOutputValid();

	}

	public class TxValidatorImpl implements TxValidator {

		private UTXOPool currentPool;

		public TxValidatorImpl(UTXOPool currentPool) {
			super();
			this.currentPool = currentPool;
		}

		@Override
		public boolean isTransactionNotInPool(UTXO currentUnSpentTransaction) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isSignatureInValid(Transaction tx, Input transactionInputEntry) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isTransactionDuplicated(List<UTXO> allTransactions, UTXO currentUnSpentTransaction) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isValueInvalid(Transaction tx, Input transactionInputEntry) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isTransactionOutputValid() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	/**
	 * @return true if: (1) all outputs claimed by {@code tx} are in the current
	 *         UTXO pool, (2) the signatures on each input of {@code tx} are
	 *         valid, (3) no UTXO is claimed multiple times by {@code tx}, (4)
	 *         all of {@code tx}s output values are non-negative, and (5) the
	 *         sum of {@code tx}s input values is greater than or equal to the
	 *         sum of its output values; and false otherwise.
	 */
	public boolean isValidTx(Transaction tx) {

		List<UTXO> allTransactions = new ArrayList<>();

		double sumOfInputTransaction = 0;

		for (Input transactionInputEntry : tx.getInputs()) {

			UTXO currentUnSpentTransaction = new UTXO(transactionInputEntry.prevTxHash,
					transactionInputEntry.outputIndex);
			// (1) all outputs claimed by {@code tx} are in the current UTXO
			// pool,
			if (isTransactionNotInPool(currentUnSpentTransaction))
				return false;
			// 2) the signatures on each input of {@code tx} are valid,
			if (isSignatureInValid(tx, transactionInputEntry))
				return false;
			// (3) no UTXO is claimed multiple times by {@code tx},
			if (isTransactionDuplicated(allTransactions, currentUnSpentTransaction))
				return false;
			allTransactions.add(currentUnSpentTransaction);
			// (4) all of {@code tx}s output values are non-negative, and
			if (isValueInvalid(tx, transactionInputEntry))
				return false;
			sumOfInputTransaction += currentPool.getTxOutput(currentUnSpentTransaction).value;
		}

		// (5) the sum of {@code tx}s input values is greater than or equal to
		// the sum of its output values; and false otherwise.
		if (getOutputTransactionSum(tx) > sumOfInputTransaction)
			return false;

		return true;
	}

	private double getOutputTransactionSum(Transaction tx) {
		double sumOfOutputTransaction = 0;
		for (Output output : tx.getOutputs()) {
			sumOfOutputTransaction += output.value;
		}
		return sumOfOutputTransaction;
	}

	/**
	 * 
	 * @param tx
	 * @param allInputTransactions
	 * @return
	 */
	private boolean isSignatureInValid(Transaction tx, Input allInputTransactions) {
		return !Crypto.verifySignature(tx.getOutput(allInputTransactions.outputIndex).address,
				tx.getRawDataToSign(allInputTransactions.outputIndex), allInputTransactions.signature);
	}

	/**
	 * 
	 * @param tx
	 * @param allInputTransactions
	 * @return
	 */
	private boolean isValueInvalid(Transaction tx, Input allInputTransactions) {
		return tx.getOutput(allInputTransactions.outputIndex).value < 0;
	}

	/**
	 * 
	 * @param allTransactions
	 * @param currentUnSpentTransaction
	 * @return
	 */
	private boolean isTransactionDuplicated(List<UTXO> allTransactions, UTXO currentUnSpentTransaction) {
		return allTransactions.contains(currentUnSpentTransaction);
	}

	/**
	 * 
	 * @param currentUnSpentTransaction
	 * @return
	 */
	private boolean isTransactionNotInPool(UTXO currentUnSpentTransaction) {
		return !currentPool.contains(currentUnSpentTransaction);
	}

	/**
	 * Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness, returning a
	 * mutually valid array of accepted transactions, and updating the current
	 * UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		List<Transaction> validTransaction = new ArrayList<>();
		for (Transaction transactionEntry : possibleTxs) {
			if (isValidTx(transactionEntry)) {
				validTransaction.add(transactionEntry);
				for (Input transactionInputEntry : transactionEntry.getInputs()) {
					UTXO spentTransaction = new UTXO(transactionInputEntry.prevTxHash,
							transactionInputEntry.outputIndex);
					currentPool.removeUTXO(spentTransaction);
				}
				int index = 0;
				for (Output transactionOutputEntry : transactionEntry.getOutputs()) {
					UTXO validatedTransaction = new UTXO(transactionEntry.getHash(), index);
					index++;
					currentPool.addUTXO(validatedTransaction, transactionOutputEntry);
				}
			}
		}

		return null;
	}

}
