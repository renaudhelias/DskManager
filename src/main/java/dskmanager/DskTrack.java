package dskmanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DskTrack {
	String header="Track-Info\r\n";
	
	private DskMaster master;

	public List<DskSector> sectors=new ArrayList<DskSector>();

	int noTrack;
	int side=0;
	int sectorSize=2;
	int nbSectors=9;
	int gap=0x2A;
	int fillerByte=0xE5;

	
	
	public DskTrack(DskMaster master) {
		this.master = master;
	}
	
	public void scan(FileInputStream fis) throws IOException {
		byte[] bufferHeader = new byte[12];
		fis.read(bufferHeader);
		header=master.arrayToString(bufferHeader);
		fis.read();fis.read();fis.read();fis.read();
		noTrack=fis.read();
		side=fis.read();
		fis.read();fis.read();
		sectorSize=fis.read();
		nbSectors=fis.read();
		gap=fis.read();
		fillerByte=fis.read();
	}
	/**
	 * Au début d'un Track-header
	 * @param fos
	 * @throws IOException 
	 */
	public void scan(FileOutputStream fos) throws IOException {
		fos.write(header.getBytes());
		fos.write(0);fos.write(0);fos.write(0);fos.write(0);
		fos.write(noTrack);
		fos.write(side);// side
		fos.write(0);fos.write(0);
		fos.write(sectorSize);
		fos.write(nbSectors);
		fos.write(gap);
		fos.write(fillerByte);
	}

	public String toString() {
		return "Track-Info "+noTrack+"\n";
	}
}
