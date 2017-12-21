#!/bin/sh
mkdir -p $2
del_libtitle='s/[[:print:]]\+ => //g'
del_address='s/ (0x[[:print:]]\+//g'
for i in `ldd $1 | sed -e "$del_address" | sed -e "$del_libtitle" | sed '/^[[:space:]]*$/d'`
do
	cp $i $2 
done
