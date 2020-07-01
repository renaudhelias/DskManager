![DIRStruct_CPC.png](DIRStruct_CPC.png)

An alternative for CPCDiskXP.exe command line (not physical 3.5 part)
and ManageDsk.exe

1. Nombre de track et side : offset 30 et 31
 PARADOS80 : 80 track et 1 side
2. Début du catalog
 idCat=2 ou idCat=4 (ex pour DOSD2 c'est 4)
 PARADOS80 : idCat=2
3. nombre de idCat par entryCat
 02 03 04.. FF
 02 00 03 00 ... FF 00 00 01 00 02
 PARADOS80 : 02 03 donc 16 idCat par entryCat
4. nombre de secteur 512 d'un idCat
 Placer les fichiers BOUM1024.txt BOUM2048.txt BOUM4096.txt et regarder le nombre d'idCat correspondant
 PARADOS : 2048<=>1 idCat donc 4*512<=>1 idCat
5. nombre de secteur dans le catalog
 Remplir la disquette de petit fichiers 1 octet tmp/test100 tmp/test101...
 PARADOS : que 2 blocs de E5 dans le track 0, 10 sector par track donc 8 track pour le catalog.
