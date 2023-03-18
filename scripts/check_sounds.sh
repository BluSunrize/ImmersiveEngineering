#!/usr/bin/bash
found_stereo=0
for f in $(find src -name \*.ogg) ; do
    echo "Checking $f"
    if ffprobe -i $f |& grep stereo; then
        found_stereo=1
        echo "$f is a stereo file!"
    fi
done
exit $found_stereo
