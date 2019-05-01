package rs.raf.os.test;

import rs.raf.os.fat.FAT16;
import rs.raf.os.fat.FATException;

public class MockFAT implements FAT16 {

	private int clusterWidth;
	private int clusterCount;

	private int[] table;

	public MockFAT(int clusterWidth) {
		this.clusterWidth = clusterWidth;
		this.clusterCount = 0xFFED;
		table = new int[clusterCount];
	}

	public MockFAT(int clusterWidth, int clusterCount) throws FATException {
		this.clusterWidth = clusterWidth;

		if (clusterCount <= 0xFFED)
			this.clusterCount = clusterCount;
		else
			throw new FATException("Can not create FAT with cluster count bigger then 0xFFED !");

		table = new int[clusterCount];

	}

	public int[] getTable() {
		return table;
	}

	public int getEndOfChain() {
		return 0xFFF8;
	}

	public int getClusterCount() {

		return clusterCount;
	}

	public int getClusterWidth() {

		return clusterWidth;
	}

	public int readCluster(int clusterID) throws FATException {
		if (clusterID < 2 || clusterID >= clusterCount + 2)
			throw new FATException("Invalid clusterID was provided");

		return table[clusterID - 2];
	}

	public void writeCluster(int clusterID, int valueToWrite) throws FATException {
		if (clusterID < 2 || clusterID >= clusterCount + 2)
			throw new FATException("Invalid clusterID was provided");

		table[clusterID - 2] = valueToWrite;

	}

	public String getString() {
		StringBuilder str = new StringBuilder("[");

		for (int i = 0; i < table.length; i++) {
			str.append(table[i]);

			if (i != table.length - 1)
				str.append("|");
			else
				str.append("]");
		}

		return str.toString();
	}

}
