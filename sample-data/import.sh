#!/usr/bin/env bash

tenant=${1:-demo_tenant}
item_storage_address=http://localhost:9130/item-storage/items
instance_storage_address=http://localhost:9130/instance-storage/instances
loan_type_storage_address=http://localhost:9130/loan-types
material_type_storage_address=http://localhost:9130/material-types
identifier_type_storage_address=http://localhost:9130/identifier-types
creator_type_storage_address=http://localhost:9130/creator-types
contributor_type_storage_address=http://localhost:9130/contributor-types
instance_format_storage_address=http://localhost:9130/instance-formats
instance_type_storage_address=http://localhost:9130/instance-types
classification_type_storage_address=http://localhost:9130/classification-types


for f in ./mtypes/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${material_type_storage_address}"
done

for f in ./loantypes/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${loan_type_storage_address}"
done

for f in ./items/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${item_storage_address}"
done

for f in ./identifiertypes/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${identifier_type_storage_address}"
done

for f in ./creatortypes/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${creator_type_storage_address}"
done

for f in ./contributortypes/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${contributor_type_storage_address}"
done

for f in ./instancetypes/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${instance_type_storage_address}"
done

for f in ./classificationtypes/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${classification_type_storage_address}"
done

for f in ./instances/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${instance_storage_address}"
done
