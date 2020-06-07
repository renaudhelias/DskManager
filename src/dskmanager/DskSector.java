package dskmanager;

import java.io.FileOutputStream;
import java.io.IOException;

public class DskSector {

	DskFile dskFile;

	int trackC;
	int sideH=0;
	int sectorIdR;
	int sectorSizeN;
	int fdc1;
	int fdc2;
	
	public DskSector(int sectorId, DskFile dskFile) {
		this.sectorIdR=sectorId;
		this.dskFile=dskFile;
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
}
