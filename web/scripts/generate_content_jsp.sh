#!/usr/bin/env bash
# Create new folder under google drive back up folder
# Usage -p 'The web folder path'
while [[ $# -gt 1 ]]
do
key="$1"

case $key in
    -p|--path)
    FRONTEND_PATH="$2"
    shift # past argument
    ;;
    *)
        # unknown option
    ;;
esac
shift # past argument or value
done

if [[ $FRONTEND_PATH == *"public"* ]]
then
    sed 's/<base[^>]*>/<base href="<%=basePath%>\/" \/>/g' "$FRONTEND_PATH"/dist/index.html > "$FRONTEND_PATH"/dist/content.jsp
fi

if [[ $FRONTEND_PATH == *"yo"* ]]
then
    cp "$FRONTEND_PATH"/dist/index.html "$FRONTEND_PATH"/dist/content.jsp
fi
rm "$FRONTEND_PATH"/dist/index.html
