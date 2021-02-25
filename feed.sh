#!/bin/bash
gunzip -c newsarticle-00001.data.gz | jq '.documents[]' | \
    java -jar vespa-http-client-jar-with-dependencies.jar --endpoint http://localhost:8080
