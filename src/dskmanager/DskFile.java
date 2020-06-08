package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DskFile {
	
	String header="EXTENDED CPC DSK File\r\nDisk-Info\r\n";
	String creator="CPCDiskXP v2.5";
	int nbTracks=40;
	int nbSides=1;
	int sizeOfTrack=19;
	
	
	File file;
	List<DskTrack> tracks=new ArrayList<DskTrack>();
	public DskFile(File currentDir, String fileName) {
		file=new File(currentDir, fileName);
	}
	/**
	 * Scan from 0 to 0x0FF
	 * @param fis
	 * @throws IOException 
	 */
	public void scan(FileInputStream fis) throws IOException {
		byte[] bufferHeader = new byte[34];
		fis.read(bufferHeader);
		header=bufferHeader.toString();
		byte[] bufferCreator = new byte[14];
		fis.read(bufferCreator);
		creator=bufferCreator.toString();
		nbTracks=fis.read();
		nbSides=fis.read();
		sizeOfTrack=fis.read();
		sizeOfTrack+=fis.read()*0x10;
	}
	
	/**
	 * Scan from 0 to 0x0FF
	 * @param fis
	 * @throws IOException 
	 */
	public void scan(FileOutputStream fos) throws IOException {
		fos.write(header.getBytes());
		fos.write(creator.getBytes());
		fos.write(nbTracks);
		fos.write(nbSides);
		fos.write(0);
		fos.write(0);//sizeOfTrack);
		int i=0;
		for (i=0;i<nbTracks;i++) {
			fos.write(sizeOfTrack);
		}
		for (;i<204;i++) {
			fos.write(0);
		}
		
	}
	
	HashMap<Integer, DskSector> catSectors = new HashMap<Integer, DskSector>();
	List<DskSector> catSectorsReferenced = new ArrayList<DskSector>();
	public void generateCatSectors() {
		int catRef=0x02;
		for (DskTrack track:tracks) {
			for (DskSector sector : track.sectors) {
				if (sector instanceof DskSectorCatalogs) {
					for (DskSectorCatalog cat:((DskSectorCatalogs)sector).cats) {
						catSectorsReferenced.addAll(cat.sectors);
					}
				} else {
					sector.cat=catRef;
					catSectors.put(catRef, sector);
					catRef++;
				}
			}
		}
	}
	public DskSector nextFreeSector() {
		for (DskSector cat : catSectors.values()) {
			if (!catSectorsReferenced.contains(cat)) {
				catSectorsReferenced.add(cat);
				return cat; 
			}
		}
		return null;
	}
	
}
