.PHONY: all build clean install test

SHELL := /usr/bin/env bash

all: build

build:
	mvn build

clean:
	mvn clean

install:
	mvn install

test:
	mvn test
