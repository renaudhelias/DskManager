package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Donne accès au Master qui s'occupe de morceau algorithme poussé
 * @author Joe
 *
 */
public class DskFile {
	
	String header="EXTENDED CPC DSK File\r\nDisk-Info\r\n";
	String creator="CPCDiskXP v2.5";
	int nbTracks=40;
	int nbSides=1;
	int sizeOfTrack=19;
	
	DskMaster master;
	File file;
	List<DskTrack> tracks=new ArrayList<DskTrack>();
	public DskFile(File currentDir, String fileName) {
		master=new DskMaster(this);
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
		header=master.arrayToString(bufferHeader);
		byte[] bufferCreator = new byte[14];
		fis.read(bufferCreator);
		creator=master.arrayToString(bufferCreator);
		nbTracks=fis.read();
		nbSides=fis.read();
		sizeOfTrack=fis.read();
		sizeOfTrack+=fis.read()*0x10;
		sizeOfTrack=fis.read(); // les 0x13 mais faux, il y a un additif je pense
		fis.skip(nbTracks-1); // les 0x13
		fis.skip(0x100-nbTracks-0x34); // les 0x00
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
	
	
}
