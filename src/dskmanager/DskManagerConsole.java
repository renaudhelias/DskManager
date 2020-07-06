package dskmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DskManagerConsole {
    DskManager dm = new DskManager();
    protected DskFile dskFile;
	public void execute(String[] args) throws IOException {
		List<String> argsList = Arrays.asList(args);
		String cmd = argsList.get(0).toLowerCase();
		String currentDir = System.getProperty("user.dir");
		if (cmd.equals("help")) {
			System.out.println("newDsk format mydisk.dsk");
			for (DskType value : DskType.values()) {
				System.out.println("format "+value.name());
			}
			System.out.println("addFileDsk mydisk.dsk {generateAMSDOSHeader} files");
		} else if (cmd.equals("newdsk") && argsList.size()==3) {
			String format=argsList.get(1).toLowerCase();
			String filename=argsList.get(2).toLowerCase();
			dskFile=dm.newDsk(new File(currentDir), filename, DskType.valueOf(format.toUpperCase()));
			System.out.println("DskFile.type="+dskFile.master.type);
		} else if (cmd.equals("addfiledsk") && argsList.size()>=3) {
			String filename=argsList.get(1).toLowerCase();
			boolean generateAMSDOSHeader=argsList.get(2).equals("generateAMSDOSHeader");
			List<String> files = new ArrayList<String>();
			if (generateAMSDOSHeader) {
				for (int i=3;i<argsList.size();i++) {
					files.add(argsList.get(i));
				}
			} else {
				for (int i=2;i<argsList.size();i++) {
					files.add(argsList.get(i));
				}
			}
			for (String file : files) {
				if (!new File(currentDir,file).exists()) {
					System.out.println("Problem : "+file+" does not exist");
					return;
				}
			}
			
			
			
			dskFile=dm.loadDsk(new File(currentDir), filename);
			System.out.println("DskFile.type="+dskFile.master.type);
			for (String file : files) {
				File fileFile = new File(file);
				System.out.println("Filename "+fileFile.getName()+" ("+fileFile.getParent()+")");
				dm.addFile(dskFile, fileFile.getParent() == null ? new File(currentDir) : new File(fileFile.getParent()), fileFile.getName(), generateAMSDOSHeader ? true : null);
			}
		} else {
			System.out.println("Wrong command "+cmd+ " ("+argsList.size()+" args)");
		}
		
		
	}

}
