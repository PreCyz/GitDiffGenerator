FROM maven:3.9.6-eclipse-temurin-11

ARG VERSION
ENV version=$VERSION
ARG GITHUB_TOKEN
ENV githubToken=$GITHUB_TOKEN

RUN apt-get update && apt-get upgrade -y && apt-get install -y --no-install-recommends curl p7zip-full git jq

WORKDIR /home/app/

RUN git clone https://github.com/PreCyz/GitDiffGenerator.git

WORKDIR /home/app/GitDiffGenerator/

RUN git checkout origin/release/java11+
COPY ./java11-release.sh /docker/java11-release.sh
RUN chmod +x /docker/java11-release.sh

ENTRYPOINT ["sh", "/docker/java11-release.sh"]