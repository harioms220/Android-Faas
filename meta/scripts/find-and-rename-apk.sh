echo $branch
file=$(find . -name '*.apk')                # find apk file
extension="${file##*.}"                     # get the extension
filename="${file%.*}"                       # get the filename
mv "$file" "${filename}-${branch}.${extension}"    # rename file by attaching branch name before extension
