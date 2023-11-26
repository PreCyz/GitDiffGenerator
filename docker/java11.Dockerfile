FROM maven:3.8.7-eclipse-temurin-11

ARG VERSION
ARG GITHUB_TOKEN

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends \
    curl \
    p7zip-full \
    git \
    jq

WORKDIR /home/app/

RUN git clone https://github.com/PreCyz/GitDiffGenerator.git

WORKDIR /home/app/GitDiffGenerator/

RUN git checkout origin/release/java11+
RUN mvn versions:set -DnewVersion=${VERSION} \
    && mvn clean package
RUN mv ./target/Gipter-${VERSION}.jar ./target/Gipter.jar \
    && 7z a ./target/11+Gipter_v${VERSION}.7z ./target/Gipter-ui-description.pdf ./target/Gipter.jar ./target/gifs.json

RUN ls -lah ./target \
    && RELEASE_NOTES=`cat ./release-notes-$VERSION.txt` \
    && JSON_STRING=$( jq -n \
                      --arg tag "v$VERSION" \
                      --arg gv "Gipter_v$VERSION" \
                      --arg body "$RELEASE_NOTES" \
                      --arg branch "release/java11+" \
                      '{tag_name:$tag,target_commitish:$branch,name:$gv,body:$body,draft:true,prerelease:false,generate_release_notes:false}' ) \
    && RELEASE_ID=$(curl \
      -X POST \
      -H "Accept: application/vnd.github+json" \
      -H "Authorization: Bearer $GITHUB_TOKEN" \
      -H "X-GitHub-Api-Version: 2022-11-28" \
      https://api.github.com/repos/PreCyz/GitDiffGenerator/releases \
      -d "$JSON_STRING" | jq '.id' ) \
    && curl -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    -H "Content-Type: application/octet-stream" \
    https://uploads.github.com/repos/PreCyz/GitDiffGenerator/releases/$RELEASE_ID/assets?name=11%2BGipter_v${VERSION}.7z \
    --data-binary "@./target/11+Gipter_v${VERSION}.7z"
