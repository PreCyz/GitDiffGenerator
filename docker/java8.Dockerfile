FROM maven:3.6.3-jdk-8-slim

ARG VERSION
ENV version=$VERSION
ARG GITHUB_TOKEN
ENV githubToken=$GITHUB_TOKEN

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
COPY ./java8-release.sh /docker/java8-release.sh
RUN chmod +x /docker/java8-release.sh

ENTRYPOINT ["sh", "/docker/java8-release.sh"]