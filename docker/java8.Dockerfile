FROM maven:3.6.3-jdk-8-slim

ARG VERSION
ARG GITHUB_TOKEN

RUN apt-get update \
    && apt-get upgrade -y \
    && mkdir -p /usr/share/man/man1 \
    && apt-get install -y --no-install-recommends software-properties-common wget gnupg curl ca-certificates \
        bzip2 zip unzip git ncftp p7zip-full jq \
    && rm -rf /var/cache/apt/archives/* \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz https://cdn.azul.com/zulu/bin/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz \
  && cd /tmp/ \
  && tar -xf /tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz \
  && rm -f /tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz

ENV JAVA_HOME=/tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64/
RUN export JAVA_HOME

WORKDIR /home/app/

RUN git clone https://github.com/PreCyz/GitDiffGenerator.git

WORKDIR /home/app/GitDiffGenerator/

RUN git checkout origin/release/java8
RUN mvn versions:set -DnewVersion=${VERSION}
RUN mvn clean package
RUN mv ./target/Gipter-${VERSION}-jar-with-dependencies.jar ./target/Gipter.jar
RUN 7z a ./target/Gipter_v${VERSION}.7z ./target/Gipter.jar ./docs/Gipter-ui-description.pdf ./docs/gifs.json
RUN RELEASE_ID=$(curl \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/repos/PreCyz/GitDiffGenerator/releases | jq --arg tag "v${VERSION}" '.[] | select(.tag_name==$tag and .draft==true) | .id') \
    && echo "Release id: ${RELEASE_ID}" \
    && RESPONSE=$(curl --request POST \
    --header "Accept: application/vnd.github+json" \
    --header "Authorization: Bearer ${GITHUB_TOKEN}" \
    --header "X-GitHub-Api-Version: 2022-11-28" \
    --header "Content-Type: application/octet-stream" \
    --url https://uploads.github.com/repos/PreCyz/GitDiffGenerator/releases/${RELEASE_ID}/assets?name=Gipter_v${VERSION}.7z \
    --data-binary "@./target/Gipter_v${VERSION}.7z") \
    && echo "${RESPONSE}"
CMD ["echo", "release java8 done"]