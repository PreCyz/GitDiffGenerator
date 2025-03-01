#!/bin/bash
#Below variables are taken from the env vars and those are set in Dockerfile
VERSION=$version
GITHUB_TOKEN=$githubToken

mvn versions:set "-DnewVersion=$VERSION"
mvn clean package
mv "./target/Gipter-$VERSION-jar-with-dependencies.jar" ./target/Gipter.jar
7z a "./target/Gipter_v$VERSION.7z" ./target/Gipter.jar ./docs/Gipter-ui-description.pdf ./docs/gifs.json

RELEASE_ID=$(curl \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/repos/PreCyz/GitDiffGenerator/releases | jq --arg tag "v$VERSION" '.[] | select(.tag_name==$tag and .draft==true) | .id' )
echo "GET releases found release with ID: $RELEASE_ID"

#RELEASE_ID=$(echo "$RESPONSE" | jq '.id' )
#echo "Release id: $RELEASE_ID"

RESPONSE=$(curl --request POST \
    --header "Accept: application/vnd.github+json" \
    --header "Authorization: Bearer $GITHUB_TOKEN" \
    --header "X-GitHub-Api-Version: 2022-11-28" \
    --header "Content-Type: application/octet-stream" \
    --url "https://uploads.github.com/repos/PreCyz/GitDiffGenerator/releases/$RELEASE_ID/assets?name=Gipter_v$VERSION.7z" \
    --data-binary "@./target/Gipter_v$VERSION.7z" )
echo "POST files to the release $RELEASE_ID response: $RESPONSE"

if test -z "$RELEASE_ID" && test -z "$RESPONSE"; then
  echo "Release Java 8 version FAILED";
else
  echo "Release Java 8 version SUCCESSFUL";
fi