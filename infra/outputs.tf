output "table_name" {
  description = "Nombre de la tabla (variable DYNAMODB_TABLE de la API)"
  value       = aws_dynamodb_table.franchises.name
}

output "table_arn" {
  description = "ARN de la tabla"
  value       = aws_dynamodb_table.franchises.arn
}

output "aws_region" {
  description = "Región (variable AWS_REGION de la API)"
  value       = var.aws_region
}

output "app_access_key_id" {
  description = "AWS_ACCESS_KEY_ID para la API"
  value       = var.create_app_user ? aws_iam_access_key.app[0].id : null
}

output "app_secret_access_key" {
  description = "AWS_SECRET_ACCESS_KEY para la API (ver con: terraform output -raw app_secret_access_key)"
  value       = var.create_app_user ? aws_iam_access_key.app[0].secret : null
  sensitive   = true
}

output "instance_id" {
  description = "ID de la instancia EC2 de la API"
  value       = aws_instance.api.id
}

output "api_url" {
  description = "URL base de la API"
  value       = "http://${aws_instance.api.public_ip}:${var.api_port}"
}

output "swagger_url" {
  description = "Swagger UI"
  value       = "http://${aws_instance.api.public_ip}:${var.api_port}/swagger-ui.html"
}
