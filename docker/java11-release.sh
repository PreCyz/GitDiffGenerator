#!/bin/bash
#Below variables are taken from the env vars and those are set in Dockerfile
VERSION=$version
GITHUB_TOKEN=$githubToken

mvn versions:set "-DnewVersion=$VERSION"
mvn clean package
mv "./target/Gipter-$VERSION.jar" ./target/Gipter.jar
7z a "./target/11+Gipter_v$VERSION.7z" ./target/Gipter.jar ./docs/Gipter-ui-description.pdf ./docs/gifs.json

RELEASE_NOTES=$(cat "./releaseNotes/release-notes-$VERSION.txt")

JSON_STRING=$(jq -n  \
    --arg tag "v$VERSION" \
    --arg gv "Gipter_v$VERSION" \
    --arg body "$RELEASE_NOTES" \
    --arg branch "release/java11+" \
    '{tag_name:$tag,target_commitish:$branch,name:$gv,body:$body,draft:true,prerelease:false,generate_release_notes:false}')

RELEASE_ID=$(curl --request POST  \
    --header "Accept: application/vnd.github+json"  \
    --header "Authorization: Bearer $GITHUB_TOKEN"  \
    --header "X-GitHub-Api-Version: 2022-11-28"  \
    --url https://api.github.com/repos/PreCyz/GitDiffGenerator/releases  \
    --data "$JSON_STRING" | jq '.id')
echo "[POST] Created release: $RELEASE_ID"

RESPONSE=$(curl --request POST  \
    --header "Accept: application/vnd.github+json"  \
    --header "Authorization: Bearer $GITHUB_TOKEN"  \
    --header "X-GitHub-Api-Version: 2022-11-28"  \
    --header "Content-Type: application/octet-stream"  \
    --url "https://uploads.github.com/repos/PreCyz/GitDiffGenerator/releases/$RELEASE_ID/assets?name=11%2BGipter_v$VERSION.7z"  \
    --data-binary "@./target/11+Gipter_v$VERSION.7z")
echo "Upload [POST] attachments for the release $RELEASE_ID - response $RESPONSE"

if test -z "$RELEASE_ID" && test -z "$RESPONSE"; then
  echo "release java11 FAILED";
else
  echo "release java11 SUCCESSFUL";
fi