#
# ECS Resources
#

# ECS cluster is only a name that ECS machines may join
resource "aws_ecs_cluster" "iaas" {

  lifecycle {
    create_before_destroy = true
  }

  name = "IAAS_${var.stack_name}"
}

# Template for container definition, allows us to inject environment
data "template_file" "ecs_iaas_task" {
  template = "${file("${path.module}/containers.json")}"

  vars {
    image             = "${var.service_image}"
  }
}

# Allows resource sharing among multiple containers
resource "aws_ecs_task_definition" "iaas" {
  family                = "iaas_${var.stack_name}"
  container_definitions = "${data.template_file.ecs_iaas_task.rendered}"
}

# Defines running an ECS task as a service
resource "aws_ecs_service" "iaas" {
  name                               = "IAAS_${var.stack_name}"
  cluster                            = "${aws_ecs_cluster.iaas.id}"
  task_definition                    = "${aws_ecs_task_definition.iaas.family}:${aws_ecs_task_definition.iaas.revision}"
  desired_count                      = "${var.desired_instance_count}"
  iam_role                           = "${var.ecs_service_role}"
  deployment_minimum_healthy_percent = "0" # allow old services to be torn down
  deployment_maximum_percent         = "100"

  load_balancer {
    elb_name       = "${aws_elb.iaas.name}"
    container_name = "interpolation-as-a-service"
    container_port = 7070
  }
}

# Load balance among all running containers
resource "aws_elb" "iaas" {
  subnets         = ["${var.subnet_id}"]

  listener {
    lb_port = 80
    lb_protocol       = "HTTP"
    instance_port     = 80
    instance_protocol = "HTTP"
  }

  cross_zone_load_balancing   = false
  idle_timeout = 3600

  tags {
    Name        = "IAAS ${var.stack_name}"
  }
}

#
# AutoScaling resources
#

# Defines a launch configuration for ECS worker, associates it with our cluster
resource "aws_launch_configuration" "ecs" {
  name = "ECS ${aws_ecs_cluster.iaas.name}"
  image_id             = "${var.aws_ecs_ami}"
  instance_type        = "${var.ecs_instance_type}"
  iam_instance_profile = "${var.ecs_instance_profile}"

  # TODO: is there a good way to make the key configurable sanely?
  key_name             = "${var.ec2_key}"
  associate_public_ip_address = true
  user_data = "#!/bin/bash\necho ECS_CLUSTER='${aws_ecs_cluster.iaas.name}' > /etc/ecs/ecs.config"
}

# Auto-scaling group for ECS workers
resource "aws_autoscaling_group" "ecs" {
  lifecycle {
    create_before_destroy = true
  }

  # Explicitly linking ASG and launch configuration by name
  # to force replacement on launch configuration changes.
  name = "${aws_launch_configuration.ecs.name}"

  launch_configuration      = "${aws_launch_configuration.ecs.name}"
  health_check_grace_period = 600
  health_check_type         = "EC2"
  desired_capacity          = "${var.desired_instance_count}"
  min_size                  = "${var.desired_instance_count}"
  max_size                  = "${var.desired_instance_count}"
  vpc_zone_identifier       = ["${var.subnet_id}"]

  tag {
    key                 = "Name"
    value               = "ECS ${aws_ecs_cluster.iaas.name}"
    propagate_at_launch = true
  }
}
