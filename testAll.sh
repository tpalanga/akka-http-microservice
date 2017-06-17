#!/usr/bin/env bash

sbt "; project accountService; clean; coverage; test; coverageReport" && \
sbt "; project newsletterService; clean; coverage; test; coverageReport" && \
sbt docker:publishLocal && \
docker-compose -f docker-compose.yml up -d && \
sbt -DCONFIG=ci.conf accountServiceTest/test