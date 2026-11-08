variable "aws_region" {
  description = "Región de AWS donde se crea la infraestructura"
  type        = string
  default     = "us-east-2"
}

variable "project_name" {
  description = "Nombre del proyecto, usado en nombres y etiquetas"
  type        = string
  default     = "franchise-api"
}

variable "table_name" {
  description = "Nombre de la tabla única de DynamoDB"
  type        = string
  default     = "franchises"
}

variable "read_capacity" {
  description = "Unidades de lectura provisionadas (el nivel gratuito de AWS cubre hasta 25)"
  type        = number
  default     = 5
}

variable "write_capacity" {
  description = "Unidades de escritura provisionadas (el nivel gratuito de AWS cubre hasta 25)"
  type        = number
  default     = 5
}

variable "create_app_user" {
  description = "Crea un usuario IAM con permisos mínimos sobre la tabla para que la API corra fuera de AWS"
  type        = bool
  default     = true
}
