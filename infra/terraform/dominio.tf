# Domínio próprio (ex.: beshuffle.com.br) com o DNS no Route 53. Liga em duas fases:
#   1. domain_name = "..."                  -> cria só a zona; troque os nameservers no registrador pelos do output.
#   2. domain_nameservers_delegated = true  -> certificado (us-east-1, validado pelo DNS), domínio no CloudFront e
#                                              registros A/AAAA do domínio e do www.
# O certificado precisa estar em us-east-1 porque é a região que o CloudFront usa para certificados.
locals {
  usar_dominio  = var.domain_name != ""
  dominio_ativo = local.usar_dominio && var.domain_nameservers_delegated
  nomes_do_site = local.dominio_ativo ? [var.domain_name, "www.${var.domain_name}"] : []
}

resource "aws_route53_zone" "site" {
  count = local.usar_dominio ? 1 : 0

  name    = var.domain_name
  comment = "BeShuffle: DNS do site"
}

resource "aws_acm_certificate" "site" {
  count    = local.dominio_ativo ? 1 : 0
  provider = aws.us_east_1

  domain_name               = var.domain_name
  subject_alternative_names = ["www.${var.domain_name}"]
  validation_method         = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

# Registros que provam ao ACM que o domínio é nosso. Ficam para sempre: o ACM usa os mesmos para renovar sozinho.
resource "aws_route53_record" "validacao" {
  for_each = local.dominio_ativo ? {
    for opcao in aws_acm_certificate.site[0].domain_validation_options : opcao.domain_name => opcao
  } : {}

  zone_id         = aws_route53_zone.site[0].zone_id
  name            = each.value.resource_record_name
  type            = each.value.resource_record_type
  records         = [each.value.resource_record_value]
  ttl             = 300
  allow_overwrite = true
}

# Espera o certificado ficar ISSUED antes de o CloudFront usar.
resource "aws_acm_certificate_validation" "site" {
  count    = local.dominio_ativo ? 1 : 0
  provider = aws.us_east_1

  certificate_arn         = aws_acm_certificate.site[0].arn
  validation_record_fqdns = [for registro in aws_route53_record.validacao : registro.fqdn]
}

# Domínio e www apontam para o CloudFront (ALIAS: funciona no domínio raiz, onde CNAME não é permitido).
resource "aws_route53_record" "site" {
  for_each = {
    for par in setproduct(local.nomes_do_site, ["A", "AAAA"]) : "${par[0]}-${par[1]}" => {
      nome = par[0]
      tipo = par[1]
    }
  }

  zone_id = aws_route53_zone.site[0].zone_id
  name    = each.value.nome
  type    = each.value.tipo

  alias {
    name                   = aws_cloudfront_distribution.this.domain_name
    zone_id                = aws_cloudfront_distribution.this.hosted_zone_id
    evaluate_target_health = false
  }
}
