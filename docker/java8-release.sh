#!/bin/bash
set -e

#Below variable is taken from the env vars and those are set in Dockerfile
GITHUB_TOKEN=$githubToken

curl \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/repos/PreCyz/GitDiffGenerator/releases > response.json

MESSAGE=$(echo "$(cat ./response.json)" | jq -c '.message')

if test -n "$MESSAGE"; then
  cat ./response.json
  echo "release java11 FAILED"
  exit
else
  VERSION=$version
  mv "./target/Gipter-$VERSION-jar-with-dependencies.jar" ./target/Gipter.jar
  7z a "./target/Gipter_v$VERSION.7z" ./target/Gipter.jar ./docs/Gipter-ui-description.pdf ./docs/gifs.json

  RELEASE_ID=$(echo "$(cat ./response.json)" | jq --arg tag "v$VERSION" '.[] | select(.tag_name==$tag and .draft==true) |.id')

  echo "GET releases found release with ID: $RELEASE_ID"

  if test -n "$RELEASE_ID" && test "$RELEASE_ID" != "null"; then
    echo "[POST] Created release: $RELEASE_ID"

    curl --request POST \
        --header "Accept: application/vnd.github+json" \
        --header "Authorization: Bearer $GITHUB_TOKEN" \
        --header "X-GitHub-Api-Version: 2022-11-28" \
        --header "Content-Type: application/octet-stream" \
        --url "https://uploads.github.com/repos/PreCyz/GitDiffGenerator/releases/$RELEASE_ID/assets?name=Gipter_v$VERSION.7z" \
        --data-binary "@./target/Gipter_v$VERSION.7z" > response.json

    MESSAGE=$(echo "$(cat response.json)" | jq -c '.message')

    if test -z "$RELEASE_ID" || test -n "$MESSAGE"; then
      echo "Here is the response message: $MESSAGE"
      echo "release java11 FAILED"
    else
      echo "POST files to the release $RELEASE_ID"
      echo "release java11 SUCCESSFUL"
    fi
  fi
fi