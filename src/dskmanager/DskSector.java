package dskmanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DskSector {

	int trackC;
	int sideH=0;
	int sectorIdR;
	int sectorSizeN=2;
	int fdc1;
	int fdc2;

	public byte[] data;

	public int cat;
	
	public DskSector(int sectorTrack,int sectorId) {
		this.trackC = sectorTrack;
		this.sectorIdR=sectorId;
	}
	

	public void scan(FileInputStream fis) throws IOException {
		trackC=fis.read();
		sideH=fis.read();
		sectorIdR=fis.read();
		sectorSizeN=fis.read();
		fdc1=fis.read();
		fdc2=fis.read();
		fis.read();fis.read();
	}
	
	public void scan(FileOutputStream fos) throws IOException {
		fos.write(trackC);//track
		fos.write(sideH);//side
		fos.write(sectorIdR);
		fos.write(sectorSizeN);
		fos.write(fdc1);//FDC 1
		fos.write(fdc2);//FDC 2
		fos.write(0);fos.write(2);
	}
	
	public void scanData(FileOutputStream fos) throws IOException {
		fos.write(data);
	}

	public void scanData(FileInputStream fis) throws IOException {
		fis.read(data);
	}
	
	public String toString() {
		return "track "+trackC+ " head " + sideH +" id:"+String.format("#%02X", sectorIdR)+"\n"+data.length+"\n";
	}

}
