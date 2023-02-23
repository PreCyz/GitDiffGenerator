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

COPY src /home/app/src
COPY pom.xml /home/app
COPY releaseNotes/release-notes-$VERSION.txt /home/app

RUN mvn -f /home/app/pom.xml versions:set -DnewVersion=$VERSION \
    && mvn -f /home/app/pom.xml clean package

COPY docs/Gipter-ui-description.pdf /home/app/target/Gipter-ui-description.pdf

RUN mv /home/app/target/Gipter-${VERSION}.jar /home/app/target/Gipter.jar \
    && 7z a /home/app/target/11+Gipter_v${VERSION}.7z /home/app/target/Gipter-ui-description.pdf /home/app/target/Gipter.jar

RUN ls -lah /home/app/target \
    && RELEASE_NOTES=`cat /home/app/release-notes-$VERSION.txt` \
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
    --data-binary "@/home/app/target/11+Gipter_v${VERSION}.7z"
