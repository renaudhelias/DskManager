package dskmanager;

/**
-------------------------------------------------------------------------------------
FORMAT           ;CAPACITY       DIRECTORIES         ;TYPE   SECTORS/TRACK     SECTORS
-------------------------------------------------------------------------------------
PARADOS 80        396k             ;128             ;  SS         ;10           ;&91-&9a
PARADOS 41        203k             ; 64             ;  SS         ;10           ;&81-&8a
PARADOS 40D       396k             ;128             ;  DS         ;10           ;&a1-&aa
ROMDOS D1         ;716k             ;128             ;  DS          9           ;&01-&09
ROMDOS D2         ;712k             ;256             ;  DS          9           ;&21-&29
ROMDOS D10        796k             ;128             ;  DS         ;10           ;&11-&1a
ROMDOS D20        792k             ;256             ;  DS         ;10           ;&31-&3a
ROMDOS D40        396k             ;128             ;  SS         ;10           ;&51-&5a
S-DOS             ;396k             ;128             ;  SS         ;10           ;&71-&7a
DATA (SS 40)      178k             ; 64             ;  SS          9           ;&c1-&c9
DATA (DS 40)      356k             ; 64             ;  DS          9           ;&c1-&c9 E
DATA (SS 80)      356k             ; 64             ;  SS          9           ;&c1-&c9 E
DATA (DS 80)      716k             ; 64             ;  DS          9           ;&c1-&c9 E
SYSTEM (SS 40)    169k             ; 64             ;  SS          9           ;&41-&49
SYSTEM (DS 40)    346k             ; 64             ;  DS          9           ;&41-&49 E
SYSTEM (SS 80)    346k             ; 64             ;  SS          9           ;&41-&49 E
SYSTEM (DS 80)    704k             ; 64             ;  DS          9           ;&41-&49 E
IBM (SS 40)       169k             ; 64             ;  SS          8           ;&01-&08
IBM (DS 40)       346k             ; 64             ;  DS          8           ;&01-&08 E
IBM (SS 80)       346k             ; 64             ;  SS          8           ;&01-&08 E
IBM (DS 80)       704k             ; 64             ;  DS          8           ;&01-&08 E
ULTRAFORM         ;203k             ; 64             ;  SS          9           ;&10-&19
-------------------------------------------------------------------------------------
 *
 * Formats marked 'E' in the format list above are ELECTRO formats (so exploited by hackers ParaDOS RSX)
 *
 * IBM : save"hello.bas" cat:nothing ! ùcpm:nothing also, 2 catList
 * PARADOS80 : track 1 contains DATA+emptyDATA, track 2 contains DATA+emptyDATA
 * PARADOS40D : track 1 contains DATA+emptyDATA, track 2 contains DATA+emptyDATA
**/
public enum DskType {
	PARADOS41, SS40, DOSD2, DOSD10, DOSD20, DOSD40, SDOS, SYSTEM, VORTEX;
}
