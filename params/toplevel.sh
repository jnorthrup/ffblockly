#!/bin/bash
#
#for i in $(ffmpeg -h|grep -e '\-\w\+\s\{2,\}show available' |cut -f 1 -d' ')
#do
#
#ffmpeg ${i}
#done
#>toplevel
mkdir -p corpus
for i in $(ffmpeg -h|awk  '$3 ~ /available/{  print $1} ')
do
ffmpeg $i >corpus/$(echo ${i//-}-$(ffmpeg $i|head -n 1|sed -e 's,:,,' -e 's,\W\+,_,g' -e 's,$,.txt,' ))
done 2>/dev/null

