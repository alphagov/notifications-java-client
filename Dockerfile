FROM maven:3-amazoncorretto-11-debian

RUN echo "Install packages"
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
