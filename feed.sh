#!/bin/bash
for i in *.data.gz; do
    gunzip -c "$i" | jq '.documents[]' | \
        java -jar vespa-http-client-jar-with-dependencies.jar --endpoint http://localhost:8080
done
