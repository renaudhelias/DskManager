![DIRStruct_CPC.png](DIRStruct_CPC.png)

An alternative for CPCDiskXP.exe command line (not physical 3.5 part)
and ManageDsk.exe

1. Nombre de track et side : offset 30 et 31
 PARADOS40D : 40 track et 2 side
2. Début du catalog
 idCat=2 ou idCat=4 (ex pour DOSD2 c'est 4)
 PARADOS40D : idCat=2
3. nombre de idCat par entryCat
 02 03 04.. FF
 02 00 03 00 ... FF 00 00 01 00 02
 PARADOS40D : 02 03 donc 16 idCat par entryCat mais sectorCount++ à 0x40
4. nombre de secteur 512 d'un idCat
 Placer les fichiers BOUM1024.txt BOUM2048.txt BOUM4096.txt et regarder le nombre d'idCat correspondant
 PARADOS40D : 2048<=>1 idCat donc 4*512<=>1 idCat
5. nombre de secteur dans le catalog
 Remplir la disquette de petit fichiers 1 octet tmp/test100 tmp/test101...
 PARADOS40D : 2 trou premier track parmis 10 sector donc 8 blocs pour le catalog.
