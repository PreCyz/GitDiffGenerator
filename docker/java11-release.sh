#!/bin/bash
set -e
#Below variables are taken from the env vars and those are set in Dockerfile
VERSION=$version
GITHUB_TOKEN=$githubToken

RELEASE_NOTES=$(cat "./releaseNotes/release-notes-$VERSION.txt")

JSON_STRING=$(jq -n  \
    --arg tag "v$VERSION" \
    --arg gv "Gipter_v$VERSION" \
    --arg body "$RELEASE_NOTES" \
    --arg branch "release/java11+" \
    '{tag_name:$tag,target_commitish:$branch,name:$gv,body:$body,draft:true,prerelease:false,generate_release_notes:false}')

curl --request POST  \
    --header "Accept: application/vnd.github+json"  \
    --header "Authorization: Bearer $GITHUB_TOKEN"  \
    --header "X-GitHub-Api-Version: 2022-11-28"  \
    --url https://api.github.com/repos/PreCyz/GitDiffGenerator/releases  \
    --data "$JSON_STRING" > response.json

RELEASE_ID=$(echo "$(cat response.json)" | jq -c '.id')

if test -n "$RELEASE_ID" && test "$RELEASE_ID" != "null"; then
  echo "[POST] Created release: $RELEASE_ID"

  mv "./target/Gipter-$VERSION.jar" ./target/Gipter.jar
  7z a "./target/11+Gipter_v$VERSION.7z" ./target/Gipter.jar ./docs/Gipter-ui-description.pdf ./docs/gifs.json

  curl --request POST  \
      --header "Accept: application/vnd.github+json"  \
      --header "Authorization: Bearer $GITHUB_TOKEN"  \
      --header "X-GitHub-Api-Version: 2022-11-28"  \
      --header "Content-Type: application/octet-stream"  \
      --url "https://uploads.github.com/repos/PreCyz/GitDiffGenerator/releases/$RELEASE_ID/assets?name=11%2BGipter_v$VERSION.7z"  \
      --data-binary "@./target/11+Gipter_v$VERSION.7z" > response.json
  echo "Upload [POST] attachments for the release $RELEASE_ID"

  MESSAGE=$(echo "$(cat response.json)" | jq -c '.message')

  if test -z "$RELEASE_ID" || test -n "$MESSAGE"; then
    echo "Here is the response message: $MESSAGE"
    echo "release java11 FAILED"
  else
    echo "release java11 SUCCESSFUL"
  fi

else
  cat response.json
  echo "release java11 FAILED"
fi