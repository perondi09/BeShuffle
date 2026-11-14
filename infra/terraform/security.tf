# Descrições de security group só aceitam ASCII.
resource "aws_security_group" "app" {
  name        = "${var.project}-api"
  description = "API BeShuffle: so recebe trafego do CloudFront na porta 8080"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_vpc_security_group_ingress_rule" "app_from_cloudfront" {
  security_group_id = aws_security_group.app.id
  description       = "CloudFront para a API"
  prefix_list_id    = data.aws_ec2_managed_prefix_list.cloudfront.id
  ip_protocol       = "tcp"
  from_port         = 8080
  to_port           = 8080
}

# Saída livre: Spotify, ECR, SSM e RDS. Sem SSH: o acesso ao servidor é pelo Session Manager.
resource "aws_vpc_security_group_egress_rule" "app_all" {
  security_group_id = aws_security_group.app.id
  description       = "Saida para internet e servicos da AWS"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

resource "aws_security_group" "db" {
  name        = "${var.project}-db"
  description = "RDS BeShuffle: so aceita a API"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_vpc_security_group_ingress_rule" "db_from_app" {
  security_group_id            = aws_security_group.db.id
  description                  = "Postgres a partir da API"
  referenced_security_group_id = aws_security_group.app.id
  ip_protocol                  = "tcp"
  from_port                    = 5432
  to_port                      = 5432
}
