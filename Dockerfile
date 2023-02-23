FROM maven:3.6.3-jdk-8

ARG VERSION
ARG GITHUB_TOKEN

RUN apt-get update \
    && apt-get upgrade -y \
    && mkdir -p /usr/share/man/man1 \
    && apt-get install -y --no-install-recommends \
        software-properties-common \
        wget \
        gnupg \
        curl \
        ca-certificates \
        bzip2 \
        zip \
        unzip \
        git \
        ncftp \
        p7zip-full \
        jq \
    && rm -rf /var/cache/apt/archives/* \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz  https://cdn.azul.com/zulu/bin/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz  \
  && cd /tmp/ \
  && tar -xf /tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz  \
  && rm -f /tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64.tar.gz

ENV JAVA_HOME=/tmp/zulu8.68.0.21-ca-fx-jdk8.0.362-linux_x64/
RUN export JAVA_HOME
RUN java -version

COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn -f /home/app/pom.xml versions:set -DnewVersion=$VERSION \
    && mvn -f /home/app/pom.xml clean package

COPY docs/Gipter-ui-description.pdf /home/app/target/Gipter-ui-description.pdf

RUN mv /home/app/target/Gipter-${VERSION}-jar-with-dependencies.jar /home/app/target/Gipter.jar \
    && 7z a /home/app/target/Gipter_v${VERSION}.7z /home/app/target/Gipter-ui-description.pdf /home/app/target/Gipter.jar

RUN ls -lah /home/app/target \
    && RELEASE_ID=$(curl \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/repos/PreCyz/GitDiffGenerator/releases | jq --arg tag "v$VERSION" '.[] | select(.tag_name==$tag and .draft==true) | .id') \
    && echo "Release id: $RELEASE_ID" \
    && curl -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    -H "Content-Type: application/octet-stream" \
    https://uploads.github.com/repos/PreCyz/GitDiffGenerator/releases/$RELEASE_ID/assets?name=Gipter_v${VERSION}.7z \
    --data-binary "@/home/app/target/Gipter_v${VERSION}.7z"
