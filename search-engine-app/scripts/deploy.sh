#!/bin/bash
# Copyright Verizon Media. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
set -euo pipefail
set -x

mvn clean package
kubectl apply -f deployments/configmap.yml -f deployments/master.yml -f deployments/container.yml -f deployments/content.yml -f deployments/headless.yml -f deployments/service.yml
while [[ $(kubectl get pods -l app=vespa -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}' | sort -u) != "True" ]]; do echo "waiting for pod" && sleep 10; done
kubectl cp target/application.zip vespa-0:/workspace
kubectl exec vespa-0 -- bash -c '/opt/vespa/bin/vespa-deploy prepare /workspace/application.zip && /opt/vespa/bin/vespa-deploy activate'
kubectl exec vespa-0 -- bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' http://localhost:19071/ApplicationStatus)" != "200" ]]; do sleep 10; done'
