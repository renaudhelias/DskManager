package dskmanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DskTrack {
	
	
	public static final String HEADER_TRACK="Track-Info\r\n";
	
	List<DskSector> sectors;

	private int noTrack;
	byte sectorSize=2;
	byte nbSectors=9;
	byte gap=0x2A;
	int fillerByte=0xE5;
	private int side=0;
	
	public DskTrack(int noTrack) {
		this.noTrack = noTrack;
	}
	
	public void scan(FileInputStream fis) {
		
	}
	/**
	 * Au début d'un Track-header
	 * @param fos
	 * @throws IOException 
	 */
	public void scan(FileOutputStream fos) throws IOException {
		fos.write(HEADER_TRACK.getBytes());
		fos.write(0);fos.write(0);fos.write(0);fos.write(0);
		fos.write(noTrack);
		fos.write(side);// side
		fos.write(0);fos.write(0);
		fos.write(sectorSize);
		fos.write(nbSectors);
		fos.write(gap);
		fos.write(fillerByte);
		
		// SECTOR INFfos
		int [] sectorId={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
		for (int j=0;j<nbSectors;j++) {
			fos.write(noTrack);//track
			fos.write(0);//side
			fos.write(sectorId[j]);
			fos.write(sectorSize);
			fos.write(0);//FDC 1
			fos.write(0);//FDC 2
			fos.write(0);fos.write(2);
		}
		// garbage
		for (int k=0;k<0x200-0x160;k++) {
			fos.write(0);					
		}
		for (int j=0;j<nbSectors;j++) {
			for (int k=0;k<computeSectorSize(sectorSize);k++) {
				fos.write(fillerByte);
			}
		}
	}
	
	private int computeSectorSize(byte sectorSize) {
		//(x"80",x"100",x"200",x"400",x"800",x"1000",x"1800");
		if (sectorSize==2) return 0x200;  
		return 0;
	}

}
