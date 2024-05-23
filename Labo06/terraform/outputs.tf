# Tells terraform which output values it should provide once the plan is applied

output "gce_instance_ip" {
  value = google_compute_instance.default.network_interface.0.access_config.0.nat_ip
}
