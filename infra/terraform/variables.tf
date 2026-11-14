variable "region" {
  description = "Região da AWS. us-east-1 é a mais barata; o CloudFront entrega o site perto do usuário."
  type        = string
  default     = "us-east-1"
}

variable "project" {
  description = "Prefixo dos nomes dos recursos."
  type        = string
  default     = "beshuffle"
}

variable "instance_type" {
  description = "EC2 da API. Graviton (ARM): t4g.small tem 2 GB, folga para a JVM. Elegível ao plano Free."
  type        = string
  default     = "t4g.small"
}

variable "db_instance_class" {
  description = "Classe do RDS. db.t4g.micro é elegível ao plano Free (Single-AZ)."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_deletion_protection" {
  description = "Impede apagar o banco por engano. Para destruir tudo, mude para false e aplique antes do destroy."
  type        = bool
  default     = true
}

variable "github_repository" {
  description = "Repositório (dono/nome) que pode fazer deploy pela role OIDC, só a partir da branch master."
  type        = string
  default     = "perondi09/BeShuffle"
}

variable "budget_email" {
  description = "E-mail que recebe os alertas do orçamento mensal. Vazio = sem orçamento."
  type        = string
  default     = ""
}

variable "monthly_budget_usd" {
  description = "Teto do orçamento mensal em dólares (alerta em 80% do real e 100% do previsto)."
  type        = number
  default     = 35
}
