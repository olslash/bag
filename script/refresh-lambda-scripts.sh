#!/bin/bash

parent_path=$( cd "$(dirname "${BASH_SOURCE}")" ; pwd -P )
cd "$parent_path"

source "./lambda_management_config.conf"

read -p "Delete and replace all lambda handlers with ${jar_path}? [yN] " -n 1 -r
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    [[ "$0" = "$BASH_SOURCE" ]] && exit 1 || return 1 # handle exits from shell or function but don't exit interactive shell
fi

delete_lambda_function() {
  aws lambda delete-function --function-name $1
}

create_lambda_function() {
    aws lambda create-function \
        --function-name $1 \
        --handler $2 \
        --runtime $handler_runtime \
        --memory $handler_memory \
        --timeout $handler_timeout \
        --role $handler_aws_role \
        --zip-file fileb://$jar_path
}

for i in `seq 0 2 $((${#handlers[@]} - 1))`; do
    name=${handlers[$i]}
    handler=${handlers[$i + 1]}

    delete_lambda_function $name
    echo "deleted function: $name"
    create_lambda_function $name $handler
    echo "created function: $name with $handler"
done
