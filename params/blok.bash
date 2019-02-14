#!/usr/bin/env bash


ffmpeg 2>/dev/null -filters|cut -s -f3 -d' '|while read; do echo '/**'$(ffmpeg 2>/dev/null -h filter=$REPLY)'*'/ $REPLY,; done >/tmp/ffenum