# No spaces in STACK_NAME
export STACK_NAME := DEV
export EC2_KEY := geotrellis-cluster
export AWS_DEFAULT_REGION := us-east-1
export SUBNET_ID := subnet-c5fefdb1

# Docker image of benchmarking service
export SERVICE_TAG := latest
export SERVICE_IMG := quay.io/lossyrob/interpolation-as-a-service:${SERVICE_TAG}

deploy:
	terraform apply \
-state="${STACK_NAME}.tfstate" \
-var 'stack_name=${STACK_NAME}' \
-var 'ec2_key=${EC2_KEY}' \
-var 'subnet_id=${SUBNET_ID}' \
-var 'service_image=${SERVICE_IMG}'

destroy:
	terraform destroy -force \
-state="${STACK_NAME}.tfstate" \
-var 'stack_name=${STACK_NAME}' \
-var 'ec2_key=${EC2_KEY}' \
-var 'subnet_id=${SUBNET_ID}' \
-var 'service_image=NA'
