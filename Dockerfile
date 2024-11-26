FROM maven:3-jdk-8

RUN \
    echo "Install Debian packages" \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
    awscli \
    make \
    git \
    gnupg \
    jq

WORKDIR /var/project
COPY . .
