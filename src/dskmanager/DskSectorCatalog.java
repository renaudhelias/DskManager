package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DskSectorCatalog {
	private DskFile dskFile;

	int jocker;
	String filename;
	List<DskSector> sectors = new ArrayList<DskSector>();
	int sectorOffset;
	
	public DskSectorCatalog(DskFile dskFile) {
		this.dskFile = dskFile;
	}
	public void scan(ByteArrayInputStream bis) throws IOException {
		jocker = bis.read();
		byte[] filename = new byte[11];
		bis.read(filename);
		this.filename = String.valueOf(filename);
		
		bis.read();bis.read();bis.read();bis.read();
		
		byte[] entriesSector = new byte[0x10];
		bis.read(entriesSector);
		
		sectors.addAll(dskFile.master.explose(entriesSector,sectors));
	}
	public void scan(FileChannel channel, String filename) throws IOException {
		channel.write(ByteBuffer.wrap(new byte[]{(byte)jocker}));
		byte [] entryFileName = realname2cpcname(filename).getBytes();
		channel.write(ByteBuffer.wrap(entryFileName));
		channel.write(ByteBuffer.wrap(new byte[]{(byte)sectorOffset}));
		channel.write(ByteBuffer.wrap(new byte[]{0,0}));
		channel.write(ByteBuffer.wrap(new byte[]{(byte)sectorOffset}));
		for (DskSector sector:sectors) {
			channel.write(ByteBuffer.wrap(new byte[]{(byte)sector.cat}));
		}
		for (int j=0;j<0x10-sectors.size();j++) {
			channel.write(ByteBuffer.wrap(new byte[]{0}));					
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

	public String toString() {
		String s="DskSectorCatalog\n"+sectors.size()+" sectors\n";
		for (DskSector sector:sectors) {
			s+="track "+sector.trackC+" head "+sector.sideH+" cat:"+String.format("#%02X", sector.cat)+" id:"+String.format("#%02X", sector.sectorIdR)+" DATA size "+sector.data.length+"\n";
		}
		return s;
	}
}
