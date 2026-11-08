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
  description = "Crea un usuario IAM con clave de acceso para correr la API fuera de AWS. No hace falta en EC2: la instancia usa un rol IAM"
  type        = bool
  default     = false
}

variable "instance_type" {
  description = "Tipo de instancia EC2 para la API"
  type        = string
  default     = "t3.micro"
}

variable "repo_url" {
  description = "Repositorio público que la instancia clona y construye"
  type        = string
  default     = "https://github.com/jesuscriss301/acenture-prueba-tecnica.git"
}

variable "repo_branch" {
  description = "Rama del repositorio a desplegar"
  type        = string
  default     = "main"
}

variable "api_port" {
  description = "Puerto público de la API"
  type        = number
  default     = 8080
}

variable "allowed_cidr" {
  description = "Rango de IPs que pueden acceder a la API"
  type        = string
  default     = "0.0.0.0/0"
}
