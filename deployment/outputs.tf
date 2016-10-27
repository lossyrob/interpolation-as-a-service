output "service_elb_ip" {
  value = "${aws_elb.iaas.dns_name}"
}
