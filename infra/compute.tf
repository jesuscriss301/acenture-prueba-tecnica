# ---------------------------------------------------------------------------
# Despliegue de la API en AWS: EC2 + Docker
#   - La instancia clona el repositorio, construye la imagen (que también
#     ejecuta las pruebas) y levanta el contenedor en el puerto 8080.
#   - No usa claves de acceso: la instancia tiene un rol IAM con permiso
#     solo sobre la tabla, y el SDK de AWS lo toma automáticamente.
# ---------------------------------------------------------------------------

data "aws_vpc" "default" {
  default = true
}

data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
}

# --- Red: solo se expone el puerto de la API ------------------------------
resource "aws_security_group" "api" {
  name        = "${var.project_name}-api-sg"
  description = "Acceso HTTP a la Franchise API"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "API HTTP"
    from_port   = var.api_port
    to_port     = var.api_port
    protocol    = "tcp"
    cidr_blocks = [var.allowed_cidr]
  }

  egress {
    description = "Salida a Internet (GitHub, Maven, DynamoDB)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# --- Identidad: rol IAM de la instancia (sin claves) ----------------------
data "aws_iam_policy_document" "ec2_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "api" {
  name               = "${var.project_name}-ec2-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume_role.json
}

# Mismo permiso mínimo que el usuario de la app: solo la tabla franchises
resource "aws_iam_role_policy" "api_table_access" {
  name   = "${var.project_name}-table-access"
  role   = aws_iam_role.api.id
  policy = data.aws_iam_policy_document.table_access.json
}

# Permite entrar a la instancia por Session Manager (consola de AWS) sin abrir SSH
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.api.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "api" {
  name = "${var.project_name}-ec2-profile"
  role = aws_iam_role.api.name
}

# --- Servidor --------------------------------------------------------------
resource "aws_instance" "api" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = var.instance_type
  iam_instance_profile        = aws_iam_instance_profile.api.name
  vpc_security_group_ids      = [aws_security_group.api.id]
  associate_public_ip_address = true

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    repo_url    = var.repo_url
    repo_branch = var.repo_branch
    aws_region  = var.aws_region
    table_name  = aws_dynamodb_table.franchises.name
    api_port    = var.api_port
  })
  user_data_replace_on_change = true

  # IMDSv2 obligatorio; hop limit 2 para que el contenedor Docker
  # pueda obtener las credenciales del rol de la instancia
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    encrypted   = true
  }

  tags = {
    Name = "${var.project_name}-api"
  }
}
