#!/bin/bash
set -x
ENDPOINT="http://localhost:8080"
CLUSTER=newsarticle

continuation=""
idx=0

while
  ((idx+=1))
  echo "$continuation"
  printf -v out "%05g" $idx
  filename=${CLUSTER}-${out}.data.gz
  echo "Fetching data..."
  token=$( curl -s "${ENDPOINT}/document/v1/?wantedDocumentCount=1000&concurrency=4&cluster=${CLUSTER}&${continuation}" \
           | tee >( gzip > ${filename} ) | jq -re .continuation )
do
  continuation="continuation=${token}"
done
