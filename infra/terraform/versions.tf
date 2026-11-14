terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project   = "BeShuffle"
      ManagedBy = "Terraform"
    }
  }
}

# Só para o certificado do CloudFront, que precisa estar em us-east-1 (o resto fica na região do projeto).
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"

  default_tags {
    tags = {
      Project   = "BeShuffle"
      ManagedBy = "Terraform"
    }
  }
}
