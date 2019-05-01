package rs.raf.os.dir;

/*
 * @author Душан Милутиновић
*/

public class File {
	private String fileName;
	private int startCluster;
	private int fileSize;

	public File(String fileName, int startCluster, int size) {
		this.fileName = fileName;
		this.startCluster = startCluster;
		this.fileSize = size;
	}

	public String getFileName() {
		return fileName;
	}

	public int getStartCluster() {
		return startCluster;
	}

	public int getSize() {
		return fileSize;
	}
}
