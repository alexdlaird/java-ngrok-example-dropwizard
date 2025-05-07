.PHONY: all build clean install test

SHELL := /usr/bin/env bash

all: test

install: build

build:
	mvn install -DskipTests=true

clean:
	mvn clean

test:
	mvn test
