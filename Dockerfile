FROM maven:3-amazoncorretto-11

RUN echo "Install packages"
RUN yum install --assumeyes awscli make git gnupg jq

WORKDIR /var/project
COPY . .
