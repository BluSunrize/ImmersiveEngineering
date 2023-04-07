for f in $(find src -name \*.ogg) ; do
    if ffprobe -i $f |& grep stereo; then
        echo $f needs update
        mv $f $f.stereo.ogg
        ffmpeg -y -i $f.stereo.ogg -ac 1 $f
        rm $f.stereo.ogg
    fi
done
