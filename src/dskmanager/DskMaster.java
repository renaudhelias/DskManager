package dskmanager;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * un DskManager, personnel à un DskFile
 * @author Joe
 *
 */
public class DskMaster {
	int [] sectorSizes = new int[] {0x80,0x100,0x200,0x400,0x800,0x1000,0x1800};
	// dictionary
//	byte[]usedSectorEntry={};
	// contient aussi C1 C2 C3 C4
	List<DskSector> allSectors = new ArrayList<DskSector>();
	List<Integer> allCatsId= new ArrayList<Integer>();
	List<DskSector> allCatsSector= new ArrayList<DskSector>();
	
	Comparator<DskSector> sectorComparator = new Comparator<DskSector>() {
		@Override
		public int compare(DskSector o1, DskSector o2) {
			if (o1.trackC<o2.trackC) {
				return -1;
			}
			if (o1.trackC>o2.trackC) {
				return 1;
			}
			if (o1.sideH<o2.sideH) {
				return -1;
			}
			if (o1.sideH>o2.sideH) {
				return 1;
			}
			if (o1.sectorIdR<o2.sectorIdR) {
				return -1;
			}
			if (o1.sectorIdR>o2.sectorIdR) {
				return 1;
			}
			return 0;
		}
	};
	
	DskType type;

	public DskMaster() {
	}
	
	DskSector find0F(DskTrack track, int sectorId) {
		for (DskSector sector:track.sectors) {
			if ((sector.sectorIdR & 0x0F)==(sectorId & 0x0F)) {
				return sector;
			}
		}
		return null;
	}
	
	/**
	 * Repli allCats au passage
	 * @param entriesSector
	 * @param sectors
	 * @return
	 */
	public List<Integer> findCatsId(byte[] entriesSector) {
		List<Integer> cats= new ArrayList<Integer>();
		int k=2;
		if (type==DskType.SS40) {
			k=2; // min(catId)
		} else if (type==DskType.DOSD2) {
			k=4; // min(catId)
		}
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, sectorComparator);

		for (DskSector sector : allCSectors) {
			if (!(sector instanceof DskSectorCatalogs)) {
				int pair=0;
				for (byte b : entriesSector) {
					pair=(pair+1)%2;
					if (type==DskType.DOSD2 && pair==0) {
						//FIXME : peut être plus gros que 0xFF normalement, moi je coupe. 
						continue;
					}
					if ((b & 0xff) == k) {
						cats.add((int)(b & 0xff));
						allCatsId.add((int)(b & 0xff));
					}
				}
				k++;
			}
		}
		if (cats.size()==0) {
			System.out.println("rien dans ce catalog");
		}
		return cats;
	}
	
	/**
	 * Repli allCats au passage
	 * @param entriesSector
	 * @param sectors
	 * @return
	 */
	public List<DskSector> findCatsSector(byte[] entriesSector) {
		List<DskSector> cats= new ArrayList<DskSector>();
		float k=2;
		if (type==DskType.SS40) {
			k=2; // min(catId)
		} else if (type==DskType.DOSD2) {
			k=4; // min(catId)
		}
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, sectorComparator);

		for (DskSector sector : allCSectors) {
			if (!(sector instanceof DskSectorCatalogs)) {
				int pair=0;
				for (byte b : entriesSector) {
					pair=(pair+1)%2;
					if (type==DskType.DOSD2 && pair==0) {
						//FIXME : peut être plus gros que 0xFF normalement, moi je coupe. 
						continue;
					}
					if ((b & 0xff) == Math.floor(k)) {
						cats.add(sector);
						allCatsSector.add(sector);
					}
				}
				//FIXME idem que moduloMod 2 de nextFreeCat()
				if (type==DskType.SS40) {
					k+=0.5;
				} else if (type==DskType.DOSD2) {
					k+=0.25;
				}
				
			}
		}
		if (cats.size()==0) {
			System.out.println("rien dans ce catalog sectors list");
		}
		return cats;
	}
	
	/**
	 * recherche un cat libre et pas dans C1-C4
	 * @return
	 */
	public NewFreeCatResult nextFreeCat() {
		NewFreeCatResult cats= new NewFreeCatResult();
		// catId à 2 car les cats C1(k==0) et C2(k==0) sont figé pour le CAT
		if (type==DskType.SS40) {
			cats.catId=2; // min(catId)
		} else if (type==DskType.DOSD2) {
			cats.catId=4; // min(catId)
		}
		int catIdModulo=0;
		int catIdModuloMod=0;
		if (type==DskType.SS40) {
			cats.catId=2; // min(catId)
			catIdModuloMod=2;
		} else if (type==DskType.DOSD2) {
			cats.catId=4; // min(catId)
			catIdModuloMod=4;
		}
		
		
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, sectorComparator);
		
		
		for (int i=0;i<allCSectors.size();i++) {
			DskSector sector= allCSectors.get(i);
			if (!(sector instanceof DskSectorCatalogs)) {
				if (!allCatsId.contains(cats.catId)) {
//					allCats.put(k,sector);
					allCatsId.add((int)cats.catId);
					
					// et le suivant est un DskSectorCatalogs
					DskSector nextSector=allCSectors.get(i+1);
					if (nextSector instanceof DskSectorCatalogs) {
						// avoir confiance au tri de la liste allCSectors
						System.out.println("galere");
					}

					allCatsSector.add(allCSectors.get(i));
					// et le suivant 1 catsId <=> 2 catsSector
					allCatsSector.add(allCSectors.get(i+1));
					if (type==DskType.DOSD2) {
						allCatsSector.add(allCSectors.get(i+2));
						allCatsSector.add(allCSectors.get(i+3));
//						allCatsSector.add(allCSectors.get(i+4));
//						allCatsSector.add(allCSectors.get(i+5));
//						allCatsSector.add(allCSectors.get(i+6));
//						allCatsSector.add(allCSectors.get(i+7));
					}
					cats.catSectors.add(allCSectors.get(i));
					// et le suivant 1 catsId <=> 2 catsSector
					cats.catSectors.add(allCSectors.get(i+1));
					if (type==DskType.DOSD2) {
						cats.catSectors.add(allCSectors.get(i+2));
						cats.catSectors.add(allCSectors.get(i+3));
//						cats.catSectors.add(allCSectors.get(i+4));
//						cats.catSectors.add(allCSectors.get(i+5));
//						cats.catSectors.add(allCSectors.get(i+6));
//						cats.catSectors.add(allCSectors.get(i+7));
					}
					return cats;
				}
				
				catIdModulo++;
				if (catIdModulo%catIdModuloMod==0) {
					cats.catId++;
				}
			}
		}
		return cats;
	}
	
	
	public String arrayToString(byte[] bufferHeader) {
		return new String(bufferHeader,Charset.forName("ISO-8859-1"));
	}
	
	public byte[] stringToArray(String realname2cpcname) {
		return realname2cpcname.getBytes(Charset.forName("ISO-8859-1"));
	}
	
	public String realname2realname(String realname) {
		return cpcname2realname(realname2cpcname(realname));
	}
	
	public String cpcname2realname(String cpcname) {
    	String realname=cpcname.substring(0,8)+"."+cpcname.substring(8,11);
    	return realname;
    }

	public String realname2cpcname(String realname) {
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

	
	
	
	public boolean catalogToCreate(int trackC, int sideH,int sectorIdR) {
		if (type==DskType.SS40) {
			if (trackC==0 && sideH==0 && (sectorIdR & 0x0F)<=4) {
				return true;
			}
		} else if (type==DskType.DOSD2) {
			if (trackC==0 && sideH==0) {
				return true;
			}
			if (trackC==0 && sideH==1 && (sectorIdR & 0x0F)<=7) {
				return true;
			}
		}
		return false;
	}

	public List<DskSectorCatalogs> buildCatalogs(List<DskTrack> tracks) {
		List<DskSectorCatalogs>catalogs= new ArrayList<DskSectorCatalogs>(); 
		if (type==DskType.SS40) {
			DskTrack track0 = tracks.get(0);
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC1));
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC2));
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC3));
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC4));
		} else if (type==DskType.DOSD2) {
			DskTrack track0 = tracks.get(0);
			DskTrack track0side1 = tracks.get(1);
			catalogs.add((DskSectorCatalogs) find0F(track0,0x21));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x22));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x23));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x24));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x25));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x26));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x27));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x28));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x29));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x21));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x22));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x23));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x24));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x25));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x26));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x27));
		}
		return catalogs;
	}

	
	public int ChecksumAMSDOS(byte[] pHeader) {
	    int Checksum = 0;
	    for (int i = 0; i < 67; i++) {
	      int CheckSumByte = pHeader[i] & 0xFF;
	      Checksum += CheckSumByte;
	    }
	    return Checksum;
	  }
	  public boolean CheckAMSDOS(byte[] pHeader) {
	    int CalculatedChecksum;
	    try {
	      CalculatedChecksum = ChecksumAMSDOS(pHeader);
	    } catch (Exception e) {
	      return false;
	    }
	    int ChecksumFromHeader = pHeader[67] & 0xFF | (pHeader[68] & 0xFF) << 8;
	    if (ChecksumFromHeader == CalculatedChecksum && ChecksumFromHeader != 0) {
	      System.out.println("Has AMSDOS header");
	      return true;
	    }
	    System.out.println("Without header");
	    return false;
	  }

	public byte[] GenerateAMSDOSHeader(String filename, long fileLength) {
		byte[] pHeader=new byte[128];
//		Byte 00: User number (value from 0 to 15 or #E5 for deleted entries)
		pHeader[0]=0x00;
//		Byte 01 to 08: filename (fill unused char with spaces)
		byte name[]=stringToArray(realname2cpcname(filename).substring(0, 8));
		System.arraycopy(name, 0, pHeader, 1,8);
//		Byte 09 to 11: Extension (fill unused char with spaces)
		byte ext[]=stringToArray(realname2cpcname(filename).substring(8, 11));
		System.arraycopy(ext, 0, pHeader, 1+8,3);
//		Byte 16: first block (tape only)
		pHeader[16]=0x00;
//		Byte 17: first block (tape only)
		pHeader[17]=0x00;
//		Byte 18: file type (0:basic 1:protected 2:binary)
		pHeader[18]=0x02;
//		Byte 21 and 22: loading address LSB first
		pHeader[21]=0x00;
		pHeader[22]=0x01;
//		Byte 23: first block (tape only?)
		pHeader[23]=0x00;
//		Byte 24 and 25: file length LSB first
		pHeader[24] = (byte) (fileLength & 0xff);  
		pHeader[25] = (byte)((fileLength & 0xff00) >> 8);  
//		Byte 26 and 27: execution address for machine code program LSB first
		pHeader[26] = 0x00;
		pHeader[27] = 0x01;
//		Byte 64 and 66: 24 bits file length LSB first. Just a copy, not used!
		pHeader[64] = (byte) (fileLength& 0xff);  
		pHeader[65] = (byte)((fileLength & 0xff00) >> 8);  
		pHeader[66] = (byte)((fileLength & 0xff0000) >> 16);  
//		Byte 67 and 68: checksum for bytes 00-66 stored LSB first
		int check=ChecksumAMSDOS(pHeader);
		pHeader[67] = (byte) (check &0xff);  
		pHeader[68] = (byte)((check &0xff00) >> 8);
//		Byte 69 to 127: undefined content, free to use
		return pHeader;
	}

}
