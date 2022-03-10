#!/bin/bash
set -e

#Parameters (repository_name,artifact_name,github_pat)
REPOSITORY_NAME = $1
ARTIFACT_NAME = $2
GITHUB_PAT = $3
if [ $# -ne 3 ]
then
echo "Invalid Arguments. Need repository_name, artifact_name, github_pat"
exit 2
fi

curl -s https://api.github.com/repos/Bahmni/$REPOSITORY_NAME/actions/artifacts | \
    jq '[.artifacts[] | select (.name == '\"$ARTIFACT_NAME\"')]' | jq -r '.[0] | .archive_download_url' | \
    xargs curl -L -o $ARTIFACT_NAME.zip -H "Authorization: token $GITHUB_PAT"
unzip -d package/resources/ $ARTIFACT_NAME.zip && rm $ARTIFACT_NAME.zip