package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DskManager {
	
	public static String HEADER="EXTENDED CPC DSK File\r\nDisk-Info\r\n";
	public static String NAME_OF_CREATOR="CPCDiskXP v2.5";
	public static String HEADER_TRACK="Track-Info\r\n";
	File dskFile;
	byte nbTracks=40;
	byte nbSides=1;
	int sizeOfTrack=19;
	byte sectorSize=2;
	byte nbSectors=9;
	byte gap=0x2A;
	int fillerByte=0xE5;
	
	
	private static DskManager instance=null;
	public static DskManager getInstance(){
		if (instance==null) {instance=new DskManager();}
		return instance;
	}
	
	public void newDsk(File currentDir, String dskName) throws IOException{
		dskFile=new File(currentDir, dskName);
		OutputStream os= new FileOutputStream(dskFile); 
		os.write(HEADER.getBytes());
		os.write(NAME_OF_CREATOR.getBytes());
		os.write(nbTracks);
		os.write(nbSides);
		os.write(0);
		os.write(sizeOfTrack);
		int i=0;
		for (i=0;i<nbTracks;i++) {
			os.write(sizeOfTrack);
		}
		
		for (;i<204;i++) {
			os.write(0);
		}
		for (i=0;i<nbTracks;i++) {
			os.write(HEADER_TRACK.getBytes());
			os.write(0);os.write(0);os.write(0);os.write(0);
			os.write(i);
			os.write(0);// side
			os.write(0);os.write(0);
			os.write(sectorSize);
			os.write(nbSectors);
			os.write(gap);
			os.write(fillerByte);
			
			// SECTOR INFOS
			int [] sectorId={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
			for (int j=0;j<nbSectors;j++) {
				os.write(i);//track
				os.write(0);//side
				os.write(sectorId[j]);
				os.write(sectorSize);
				os.write(0);//FDC 1
				os.write(0);//FDC 2
				os.write(0);os.write(2);
			}
			// garbage
			for (int k=0;k<0x200-0x160;k++) {
				os.write(0);					
			}
			for (int j=0;j<nbSectors;j++) {
				for (int k=0;k<computeSectorSize(sectorSize);k++) {
					os.write(fillerByte);
				}
			}
		}
		os.close();
	}
	private int computeSectorSize(byte sectorSize) {
		//(x"80",x"100",x"200",x"400",x"800",x"1000",x"1800");
		if (sectorSize==2) return 0x200;  
		return 0;
	}

	public void addFile(File currentDir, String fileName, boolean generateAMSDOSHeader) throws IOException {
		File dskFileEntry=new File(currentDir, fileName);
		long size=dskFileEntry.length();
		int nbEntry = (int)(size/(16*1024))+1; // each 16KB
		int nbEntryLast = (int)(size%(16*1024)); // each 16KB
		
		// search entry free space
		FileInputStream fis = new FileInputStream(dskFile);
		fis.skip(0x100); //header
		fis.skip(0x100); // first Track-info
		byte [] entryFileName=new byte [8+3];
		int jocker=fis.read(); // jocker equals 0
		fis.read(entryFileName);
		fis.read();// nbEntry
		fis.read();fis.read();
		byte entryFileSize;
		entryFileSize=(byte) fis.read();
		byte [] entrySectors = new byte[0x10];
		fis.read(entrySectors);
		fis.close();
				
		if (jocker == fillerByte) {
			RandomAccessFile fos = new RandomAccessFile(dskFile, "rw");
			FileChannel channel = fos.getChannel();
			channel.position(0x200);
			for (int i=0;i<nbEntry;i++) {
				channel.write(ByteBuffer.wrap(new byte[]{0x00}));//jocker
				entryFileName=realname2cpcname(fileName).getBytes();
				channel.write(ByteBuffer.wrap(entryFileName));
				channel.write(ByteBuffer.wrap(new byte[]{(byte)i}));
				channel.write(ByteBuffer.wrap(new byte[]{0,0}));
				if (i==nbEntry-1) {
					channel.write(ByteBuffer.wrap(new byte[]{(byte) nbEntryLast})); // entrySize
				} else {
					channel.write(ByteBuffer.wrap(new byte[]{(byte) nbEntryLast})); // entrySize
				}
				channel.write(ByteBuffer.wrap(entrySectors));
			}
			fos.close();
		}
		
		
		
	
	
	}
	
	
	public static String realname2cpcname(String realname) {
    	String cpcname = realname.toUpperCase();
    	if (cpcname.contains(".")) {
            int point = cpcname.indexOf(".");
            String filename = cpcname.substring(0, point);
            filename = filename + "        ";
            filename = filename.substring(0, 8);
            String extension = cpcname.substring(point + 1,
                    cpcname.length());
            extension = extension + "   ";
            extension = extension.substring(0, 3);

            cpcname = filename + extension;
        } else {
            cpcname = cpcname + "        " + "   ";
            cpcname = cpcname.substring(0, 8 + 3);
        }
    	return cpcname;
    }
	
}
