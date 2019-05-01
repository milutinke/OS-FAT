package rs.raf.os.test;

import java.util.ArrayList;
import java.util.Arrays;

import rs.raf.os.dir.AbstractDirectory;
import rs.raf.os.dir.File;
import rs.raf.os.dir.DirectoryException;
import rs.raf.os.disk.Disk;
import rs.raf.os.fat.FAT16;

/*
 * @author Душан Милутиновић
*/

public class MockDirectory extends AbstractDirectory {
	private ArrayList<File> directoriums;

	public MockDirectory(FAT16 fat, Disk disk) {
		super(fat, disk);
		directoriums = new ArrayList<>();
	}

	public boolean writeFile(String name, byte[] data) {
		File delete = null;

		for (File file : directoriums)
			if (file.getFileName().equals(name)) {
				delete = file;
				break;
			}

		if (delete != null)
			deleteFile(delete.getFileName());

		if (data.length > getUsableFreeSpace())
			return false;

		int clusterSize = fat.getClusterWidth() * disk.getSectorSize();
		int neededClusters = data.length / clusterSize + (data.length % clusterSize == 0 ? 0 : 1);
		int previousCluster = 0;

		File file = null;

		// Za svaki potrebni klaster 
		for (int i = 0; i < neededClusters; i++) {
			
			// Prolazimo kroz sve klastere
			for (int j = 0; j < fat.getClusterCount(); j++) {

				// Kada nadjemo na prvi koji je slobodan klaster
				if (((MockFAT) fat).getTable()[j] == 0) {
					// Ako ne postoji fajl napravimo ga
					if (file == null)
						directoriums.add(file = new File(name, j, data.length));

					else
						((MockFAT) fat).getTable()[previousCluster] = j + 2;

					// Predpostavljamo da je ovaj klaster zadnji klaster
					((MockFAT) fat).getTable()[j] = fat.getEndOfChain();
					
					// Cuvamo trenutni klaster kao pretnodni
					previousCluster = j;
					
					// Upisujemo podatke u trenutni klster
					disk.writeSectors(j * fat.getClusterWidth(), fat.getClusterWidth(),
							Arrays.copyOfRange(data, i * clusterSize, i * clusterSize + clusterSize));

					break;
				}

			}
		}

		return true;
	}

	public byte[] readFile(String name) throws DirectoryException {
		int currentCluster = -1;
		byte[] data = null;

		for (File file : directoriums)
			if (file.getFileName().equals(name)) {
				data = new byte[file.getSize()];
				currentCluster = file.getStartCluster();
			}

		if (currentCluster == -1)
			throw new DirectoryException("There is no file with that name on the disk!");

		int i = 0;

		// Citamo do poslednjeg klastera
		while (((MockFAT) fat).getTable()[currentCluster] != fat.getEndOfChain()) {

			for (byte b : disk.readSectors(currentCluster * fat.getClusterWidth(), fat.getClusterWidth()))
				if (i < data.length)
					data[i++] = b;

			currentCluster = ((MockFAT) fat).getTable()[currentCluster] - 2;
		}

		// Citamo iz poslednjeg klastera
		for (byte b : disk.readSectors(currentCluster * fat.getClusterWidth(), fat.getClusterWidth()))
			if (i < data.length)
				data[i++] = b;

		return data;
	}

	public void deleteFile(String name) throws DirectoryException {
		int currentCluster = -1;
		int nextCluster = -1;
		int i = 0;

		for (i = 0; i < directoriums.size(); i++) {
			if (directoriums.get(i).getFileName().equals(name)) {
				currentCluster = directoriums.get(i).getStartCluster();
				directoriums.remove(i);
				break;
			}

		}

		if (currentCluster == -1)
			throw new DirectoryException("There is no file with that name on the disk!");

		while (((MockFAT) fat).getTable()[currentCluster] != fat.getEndOfChain()) {
			nextCluster = ((MockFAT) fat).getTable()[currentCluster] - 2;

			((MockFAT) fat).getTable()[currentCluster] = 0;

			currentCluster = nextCluster;
		}

		((MockFAT) fat).getTable()[currentCluster] = 0;

	}

	public String[] listFiles() {
		String[] string = new String[directoriums.size()];

		for (int i = 0; i < directoriums.size(); i++)
			string[i] = directoriums.get(i).getFileName();

		return string;
	}

	public int getUsableTotalSpace() {
		int fatSize = fat.getClusterWidth() * disk.getSectorSize() * fat.getClusterCount();
		int diskSize = disk.diskSize();

		return ((diskSize <= fatSize) ? diskSize : fatSize);
	}

	public int getUsableFreeSpace() {
		int clusterSize = fat.getClusterWidth() * disk.getSectorSize();
		int free = 0;
		int diskSize = disk.diskSize();

		for (int cluster : ((MockFAT) fat).getTable()) {
			if (cluster == 0)
				free += clusterSize;
			else
				diskSize -= clusterSize;
		}

		return free <= diskSize ? free : diskSize;
	}

}
