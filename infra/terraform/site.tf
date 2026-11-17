resource "aws_s3_bucket" "site" {
  bucket        = "${var.project}-site-${data.aws_caller_identity.current.account_id}"
  force_destroy = true
}

resource "aws_s3_bucket_public_access_block" "site" {
  bucket                  = aws_s3_bucket.site.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# O bucket é privado: só esta distribuição do CloudFront lê (Origin Access Control).
resource "aws_s3_bucket_policy" "site" {
  bucket = aws_s3_bucket.site.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "SoOCloudFrontLe"
      Effect    = "Allow"
      Principal = { Service = "cloudfront.amazonaws.com" }
      Action    = "s3:GetObject"
      Resource  = "${aws_s3_bucket.site.arn}/*"
      Condition = {
        StringEquals = { "AWS:SourceArn" = aws_cloudfront_distribution.this.arn }
      }
    }]
  })
}

resource "aws_cloudfront_origin_access_control" "site" {
  name                              = "${var.project}-site"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# Rotas do React (/perfil, /entrar...) não são arquivos: devolvem o index.html. O www vira o domínio sem www.
# O replace tira o CRLF que o Git do Windows põe no checkout; sem ele, todo plan quer atualizar a função.
resource "aws_cloudfront_function" "spa" {
  name    = "${var.project}-spa"
  runtime = "cloudfront-js-2.0"
  publish = true
  code    = replace(file("${path.module}/spa.js"), "\r\n", "\n")
}

resource "aws_cloudfront_distribution" "this" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "BeShuffle: site no S3 e /api no EC2"
  aliases             = local.nomes_do_site
  default_root_object = "index.html"
  # Inclui as bordas da América do Sul; o plano Free cobre 1 TB/mês de saída.
  price_class = "PriceClass_All"

  origin {
    origin_id                = "site"
    domain_name              = aws_s3_bucket.site.bucket_regional_domain_name
    origin_access_control_id = aws_cloudfront_origin_access_control.site.id
  }

  origin {
    origin_id   = "api"
    domain_name = aws_eip.app.public_dns

    custom_origin_config {
      http_port              = 8080
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  default_cache_behavior {
    target_origin_id           = "site"
    viewer_protocol_policy     = "redirect-to-https"
    allowed_methods            = ["GET", "HEAD"]
    cached_methods             = ["GET", "HEAD"]
    compress                   = true
    cache_policy_id            = data.aws_cloudfront_cache_policy.optimized.id
    response_headers_policy_id = aws_cloudfront_response_headers_policy.site.id

    function_association {
      event_type   = "viewer-request"
      function_arn = aws_cloudfront_function.spa.arn
    }
  }

  # API: sem cache, com cookies, headers (X-XSRF-TOKEN) e query string.
  ordered_cache_behavior {
    path_pattern             = "/api/*"
    target_origin_id         = "api"
    viewer_protocol_policy   = "redirect-to-https"
    allowed_methods          = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
    cached_methods           = ["GET", "HEAD"]
    compress                 = true
    cache_policy_id          = data.aws_cloudfront_cache_policy.disabled.id
    origin_request_policy_id = data.aws_cloudfront_origin_request_policy.all_viewer_except_host.id
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  # Com domínio próprio: certificado do ACM e TLS 1.2 no mínimo (prefere 1.3). Sem domínio: o certificado padrão
  # do *.cloudfront.net, que só aceita a política TLSv1.
  viewer_certificate {
    cloudfront_default_certificate = !local.dominio_ativo
    acm_certificate_arn            = local.dominio_ativo ? aws_acm_certificate_validation.site[0].certificate_arn : null
    ssl_support_method             = local.dominio_ativo ? "sni-only" : null
    minimum_protocol_version       = local.dominio_ativo ? "TLSv1.2_2021" : "TLSv1"
  }
}
