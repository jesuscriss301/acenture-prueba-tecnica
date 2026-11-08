# ---------------------------------------------------------------------------
# Tabla única (single-table design)
#   Franquicia -> PK = FRANCHISE#{id}, SK = METADATA
#   Sucursal   -> PK = FRANCHISE#{id}, SK = BRANCH#{branchId}
#   Producto   -> PK = FRANCHISE#{id}, SK = BRANCH#{branchId}#PRODUCT#{productId}
# ---------------------------------------------------------------------------
resource "aws_dynamodb_table" "franchises" {
  name           = var.table_name
  billing_mode   = "PROVISIONED"
  read_capacity  = var.read_capacity
  write_capacity = var.write_capacity
  hash_key       = "PK"
  range_key      = "SK"

  attribute {
    name = "PK"
    type = "S"
  }

  attribute {
    name = "SK"
    type = "S"
  }

  server_side_encryption {
    enabled = true
  }
}

# ---------------------------------------------------------------------------
# Usuario de la aplicación: solo puede operar sobre esta tabla
# (mínimo privilegio). Se usa cuando la API corre fuera de AWS.
# ---------------------------------------------------------------------------
resource "aws_iam_user" "app" {
  count = var.create_app_user ? 1 : 0
  name  = "${var.project_name}-app"
}

data "aws_iam_policy_document" "table_access" {
  statement {
    sid    = "FranchiseTableAccess"
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:DeleteItem",
      "dynamodb:Query",
    ]
    resources = [aws_dynamodb_table.franchises.arn]
  }
}

resource "aws_iam_user_policy" "app_table_access" {
  count  = var.create_app_user ? 1 : 0
  name   = "${var.project_name}-table-access"
  user   = aws_iam_user.app[0].name
  policy = data.aws_iam_policy_document.table_access.json
}

resource "aws_iam_access_key" "app" {
  count = var.create_app_user ? 1 : 0
  user  = aws_iam_user.app[0].name
}
