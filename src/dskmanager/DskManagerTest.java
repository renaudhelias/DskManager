package dskmanager;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class DskManagerTest {

	DskManager dm = DskManager.getInstance();
	
	File currentDir = new File(new File("src/"+DskManagerTest.class.getName().replace('.', '/')+".class").getAbsolutePath()).getParentFile();
	
	@Test
	public void test() throws InterruptedException {
		try {
			
			System.out.println(currentDir);
			System.out.println(new File(currentDir,"jdvpa10_test1.dsk").delete());
			Runtime.getRuntime().exec("CPCDiskXP -File main2.bin -AddToNewDsk jdvpa10_test1.dsk",null,currentDir).waitFor();

			FileInputStream fis = new FileInputStream(new File(currentDir,"jdvpa10_test1.dsk"));
			
			assertEquals(fis.read(),69);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("exe not found");
		}
	}
	
	@Test
	public void testDMCreateAddDsk() throws IOException {
		System.out.println(new File(currentDir,"jdvpa10_test2.dsk").delete());
		
		DskFile toto = dm.newDsk(currentDir, "jdvpa10_test2.dsk", DskType.SS40);
		dm.addFile(toto,currentDir,"main2.bin",true);
		compare(currentDir, "jdvpa10_test2.dsk", "jdvpa10_test1.dsk");
	}

	@Test
	public void testDMCreateAddDskDOSD2() throws IOException {
		System.out.println(new File(currentDir,"jdvpa10_test4.dsk").delete());
		
		DskFile toto = dm.newDsk(currentDir, "jdvpa10_test4.dsk", DskType.DOSD2);
		dm.addFile(toto,currentDir,"main2.bin",false);
		compare(currentDir, "jdvpa10_test4.dsk", "jdvpa10_test5.dsk");
	}

	@Test
	public void testDMLoadDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		assertEquals(dskFile.tracks.size(),40);
		assertEquals(dskFile.nbSides,1);
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.side,0);
		assertEquals(track0.nbSectors,9);
		assertEquals(track0.sectorSize,2);
		assertEquals(track0.sectors.size(),9);
		DskTrack track1=dskFile.tracks.get(1);
		assertEquals(track1.side,0);
	}

	@Test
	public void testDMLoadDskDOSD2() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "dosd2.dsk");
		assertEquals(dskFile.tracks.size(),80*dskFile.nbSides);
		assertEquals(dskFile.nbSides,2);
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.nbSectors,9);
		DskSectorCatalogs track0secC1=(DskSectorCatalogs)dskFile.master.find0F(track0,0xC1);
		DskSectorCatalog cat0 = track0secC1.cats.get(0) ;
		assertEquals(track0.sectors.size(),9);
		DskSector sector0 = cat0.catsSector.get(0);
		assertNotNull(sector0);
		assertEquals(cat0.catsSector.size(),4);
		assertEquals(cat0.catsId.size(),1);
		assertEquals(track0.side,0);
		DskTrack track1=dskFile.tracks.get(1);
		assertEquals(track1.side,1);
		assertEquals(track0.sectorSize,2);
		assertEquals(track0.sectors.size(),9);
		DskSectorCatalogs cat00 = (DskSectorCatalogs)(track0.sectors.get(0));
		assertEquals(cat00.cats.get(0).catsId.size(),1);
	}
	
	@Test
	public void testDMLoadDskCatalog() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "jdvpa10_test1.dsk");
		assertEquals(dskFile.tracks.size(),40);
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.nbSectors,9);
		DskSectorCatalogs track0secC1=(DskSectorCatalogs)dskFile.master.find0F(track0,0xC1);
		DskSectorCatalog cat0 = track0secC1.cats.get(0) ;
		assertEquals(track0.sectors.size(),9);
		DskSector sector0 = cat0.catsSector.get(0);
		assertNotNull(sector0);
		assertEquals(cat0.catsSector.size(),10*2);
		assertEquals(cat0.catsId.size(),10);
	}

	private void compare(File currentDir, String file1, String file2) throws IOException {
		FileInputStream fis1 = new FileInputStream(new File(currentDir,file1));
		FileInputStream fis2 = new FileInputStream(new File(currentDir,file2));
		int offset=0;
		while (fis1.available()>0 && fis2.available()>0) {
			offset++;
			assertEquals("at offset "+String.format("#%02X",offset),fis1.read(),fis2.read());
		}
		assertTrue(fis1.available()==0 && fis2.available()==0);
		fis1.close();
		fis2.close();
	}
	
	
	@Test
	public void testDMReadDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		new File(currentDir,"MATRIX8F.SKS").delete();
		File r1=dm.readFile(dskFile,currentDir,"MATRIX8F.SKS");
		new File(currentDir,"CHIEFTAI.SKS").delete();
		File r2=dm.readFile(dskFile,currentDir,"CHIEFTAI.SKS");
		new File(currentDir,"PAN.SKS").delete();
		File r3=dm.readFile(dskFile,currentDir,"PAN     .SKS");
		new File(currentDir,"THRONES.SKS").delete();
		File r4=dm.readFile(dskFile,currentDir,"THRONES .SKS");
		assertNotNull(r1);
		assertTrue(r1.exists());
		assertNotNull(r2);
		assertTrue(r2.exists());
		assertNotNull(r3);
		assertTrue(r3.exists());
		assertNotNull(r4);
		assertTrue(r4.exists());
	}
	
	@Test
	public void testDMEraseDsk() throws IOException {
		DskFile dskFile=dm.newDsk(currentDir, "jdvpa10_test3.dsk", DskType.SS40);
		File original=new File(currentDir,"main.bin");
		File tmp=new File(currentDir,"main3.bin");
		tmp.delete();
		
		InputStream in = new BufferedInputStream(new FileInputStream(original));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
		  
        byte[] buffer = new byte[1024];
        int lengthRead;
        while ((lengthRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, lengthRead);
            out.flush();
        }
		out.close();
		in.close();
		
		dm.addFile(dskFile, currentDir, "main3.bin", false);
		File r1=dm.readFile(dskFile,currentDir,"MAIN3   .BIN");
		assertNotNull(r1);
		assertTrue(r1.exists());
		tmp.delete();
		dm.eraseFile(dskFile, "MAIN3   .BIN");
		r1=dm.readFile(dskFile,currentDir,"MAIN3   .BIN");
		assertNull(r1);
		
		Set<String> list=dm.listFiles(dskFile).keySet();
		for(String notErased:list) {
			if (notErased.equals("MAIN3.BIN   ")) {
				fail("still here");
			}
		}
	}

	@Test
	public void testDMListDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		LinkedHashMap<String, ByteArrayOutputStream> listWithDATA = dm.listFiles(dskFile);
		Set<String> list=listWithDATA.keySet();
		assertNotNull(list);
		assertTrue(list.contains("TRON    .BAS"));
		assertTrue(list.contains("MATRIX8F.SKS"));
		assertEquals(listWithDATA.get("TRON    .BAS").size(),1024);
	}
	
	@Test
	public void testDMListDskDOSD2() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "jdvpa10_test5.dsk");
		LinkedHashMap<String, ByteArrayOutputStream> listWithDATA = dm.listFiles(dskFile);
		Set<String> list=listWithDATA.keySet();
		assertNotNull(list);
		assertTrue(list.contains("MAIN2   .BIN"));
		assertEquals(listWithDATA.get("MAIN2   .BIN").size(),10240);
		
		File fichier = dm.readFile(dskFile, currentDir, "MAIN2   .BIN");
		assertEquals(fichier.getName(),"MAIN2   .BIN");
		assertEquals(fichier.length(),10240);
	}
	
	@Test
	public void testDOSD2createNFiles() throws IOException {
		File tmpFolder = new File("tmp");
		tmpFolder.mkdirs();
		for (int i=100;i<1000;i++) {
			File f =new File(tmpFolder,"test"+i);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(0xAA);
			fos.close();
		}
	}
	
	@Test
	public void testRealname2realname() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "jdvpa10_test5.dsk");
		DskMaster master = new DskMaster();
		String realname="ETOILE  .BAS";
		assertEquals(master.realname2realname(realname),"ETOILE  .BAS");
		realname="etoile.bas";
		assertEquals(master.realname2realname(realname),"ETOILE  .BAS");
	}
	
	@Test
	public void testBoumDOSD2() throws IOException {
		File boum = new File(currentDir, "boum.txt");
		assertEquals(boum.length(),600000);
		DskFile dskFile = dm.newDsk(currentDir, "jdvpa10_test6.dsk", DskType.DOSD2);
		dm.addFile(dskFile, currentDir, "boum.txt", false);
		LinkedHashMap<String, ByteArrayOutputStream> list = dm.listFiles(dskFile);
		ByteArrayOutputStream fileEntry = list.get("BOUM    .TXT");
		//FIXME
		assertEquals(fileEntry.toByteArray().length, 600000);
		
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);
		int length=0;
		int count=0;
		int countCatIds=0;
		int countCatalog=0;
		List<DskSector> sectorUsed = new ArrayList<DskSector>();
		for (DskSectorCatalogs cats: catalogsC1C4) {
			countCatalog++;
			for (DskSectorCatalog cat:cats.cats) {
				countCatIds++;
				for (DskSector sector:cat.catsSector) {
					length+=sector.data.length;
					count++;
					sectorUsed.add(sector);
				}
			}
		}
		// 37 catIds *4 *512
		// 37 "BOUM    TXT" occurencies OK
		assertEquals(countCatIds,37);
		assertEquals(dskFile.master.allCatsId.size(),293);
		assertTrue(dskFile.master.allCatsId.size()/countCatIds<=16);
		assertEquals(dskFile.master.allCatsSector.size(),1172);
		assertEquals(countCatalog,(9+7));
		assertEquals(count,1172);
		assertEquals(length, 600000);
		assertEquals(length/count, 511);
		int lengthAllCatsSector=0;
		for (DskSector sector : dskFile.master.allCatsSector) {
			if (!sectorUsed.contains(sector)) {
				System.out.println(sector.toString());
			}
			lengthAllCatsSector+=sector.data.length;
		}
		assertEquals(lengthAllCatsSector, 600000);
		
		assertEquals(dskFile.master.allCatsSector.size(),count);
	}
	
	@Test
	public void testDOSD10Boum() throws IOException {
		DskFile dskFile = dm.newDsk(currentDir, "jdvpa10_test7.dsk", DskType.DOSD10);
		dm.addFile(dskFile, currentDir, "boum.txt", false);
		dskFile = dm.loadDsk(currentDir, "jdvpa10_test7.dsk");
		LinkedHashMap<String, ByteArrayOutputStream> list = dm.listFiles(dskFile);
		ByteArrayOutputStream fileEntry = list.get("BOUM    .TXT");
		assertEquals(fileEntry.toByteArray().length, 600064);
		
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);
		List<DskSector> sectorUsed = new ArrayList<DskSector>();
		int length=0;
		int count=0;
		int countCatIds=0;
		int countCatalog=0;
		for (DskSectorCatalogs cats: catalogsC1C4) {
			countCatalog++;
			for (DskSectorCatalog cat:cats.cats) {
				countCatIds++;
				for (DskSector sector:cat.catsSector) {
					length+=sector.data.length;
					count++;
					sectorUsed.add(sector);
				}
			}
		}
		assertEquals(countCatIds,37);
		assertEquals(dskFile.master.allCatsId.size(),293);
		assertTrue(dskFile.master.allCatsId.size()/countCatIds<=16);
		assertEquals(dskFile.master.allCatsSector.size(),1172);
		assertEquals(countCatalog,8);
		assertEquals(count,1172);
		assertEquals(length, 600064);
		assertEquals(length/count, 512);
	}
	
	@Test
	public void testDOSD10Test100() throws IOException {
		DskFile dskFile = dm.newDsk(currentDir, "jdvpa10_test7.dsk", DskType.DOSD10);
		assertEquals(dskFile.master.allCatsId.size(),0);
		assertEquals(dskFile.master.allCatsSector.size(),0);
		dm.addFile(dskFile, currentDir, "test100", false);
		assertEquals(dskFile.master.allCatsId.size(),1);
		assertEquals(dskFile.master.allCatsSector.size(),4);
		LinkedHashMap<String, ByteArrayOutputStream> list = dm.listFiles(dskFile);
		assertEquals(dskFile.master.allCatsId.size(),1);
		ByteArrayOutputStream fileEntry = list.get("TEST100 .   ");
		assertEquals(fileEntry.toByteArray().length, 1);
		
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);
		List<DskSector> sectorUsed = new ArrayList<DskSector>();
		int length=0;
		int count=0;
		int countCatIds=0;
		int countCatalog=0;
		for (DskSectorCatalogs cats: catalogsC1C4) {
			countCatalog++;
			for (DskSectorCatalog cat:cats.cats) {
				countCatIds++;
				for (DskSector sector:cat.catsSector) {
					length+=sector.data.length;
					count++;
					sectorUsed.add(sector);
					//break;
				}
			}
		}
		assertEquals(countCatIds,1);
		assertEquals(dskFile.master.allCatsId.size(),1);
		assertTrue(dskFile.master.allCatsId.size()/countCatIds<=16);
		assertEquals(dskFile.master.allCatsSector.size(),4);
		assertEquals(countCatalog,8);
		assertEquals(count,4);
		assertEquals(length, 1);
		assertEquals(length/count, 0);
		
		int lengthAllCatsSector=0;
		for (DskSector sector : dskFile.master.allCatsSector) {
			if (!sectorUsed.contains(sector)) {
				System.out.println(sector.toString());
			}
			lengthAllCatsSector+=sector.data.length;
		}
		assertEquals(lengthAllCatsSector, 1);
		assertEquals(dskFile.master.allCatsSector.size(),count);
	}
	
	@Test
	public void testDOSD10() throws IOException {
		DskFile dskFile = dm.loadDsk(currentDir, "jdvpa10_test8.dsk");
		assertEquals(dskFile.master.type,DskType.DOSD10);
	}

	@Test
	public void testDOSD40() throws IOException {
		DskFile dskFile = dm.newDsk(currentDir, "jdvpa10_test9.dsk", DskType.DOSD40);
		dm.addFile(dskFile, currentDir, "boum4096.txt", false);
		dskFile = dm.loadDsk(currentDir, "jdvpa10_test9.dsk");
		LinkedHashMap<String, ByteArrayOutputStream> list = dm.listFiles(dskFile);
		ByteArrayOutputStream fileEntry = list.get("BOUM4096.TXT");
		assertEquals(fileEntry.toByteArray().length, 4096);
		
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);
		List<DskSector> sectorUsed = new ArrayList<DskSector>();
		int length=0;
		int count=0;
		int countCatIds=0;
		int countCatalog=0;
		for (DskSectorCatalogs cats: catalogsC1C4) {
			countCatalog++;
			for (DskSectorCatalog cat:cats.cats) {
				countCatIds+=cat.catsId.size();
				for (DskSector sector:cat.catsSector) {
					length+=sector.data.length;
					count++;
					sectorUsed.add(sector);
				}
			}
		}
		assertEquals(dskFile.master.allCatsId.size(),2);
		assertEquals(dskFile.master.allCatsSector.size(),4*2);
		assertEquals(countCatIds,2);
		assertEquals(countCatalog,8);
		assertEquals(count,8);
		assertEquals(length, 4096);
		assertEquals(length/count, 512);
	}

}
