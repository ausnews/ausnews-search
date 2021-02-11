#!/bin/bash

helm install -f prometheus-stack.yml prometheus-grafana prometheus-community/kube-prometheus-stack --namespace monitoring
kubectl apply -f master-podmonitor.yml
