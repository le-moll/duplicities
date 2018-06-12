# duplicities

use at your own risk :)

Domaci ukol - hledani duplicitnich souboru

Paralelizace resena formou producent / n konzumeru. Jednotlive tasky jsou ukladany do fronty odkud si je konzumenti vybiraji.

Synchorinizace pro ukladani Hash pomoci synchronised u methody save...

Synchorinizace pro tasky je na urovni samotne queue - ArrayBlockingQueue z java concurent.

Pocet vlaken je v kodu.


