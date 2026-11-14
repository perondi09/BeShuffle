output "site_url" {
  description = "Endereço do site (HTTPS pelo CloudFront)."
  value       = local.dominio_ativo ? "https://${var.domain_name}" : "https://${aws_cloudfront_distribution.this.domain_name}"
}

output "name_servers" {
  description = "Nameservers do Route 53: troque no registrador do domínio (ex.: painel da HostGator) por estes."
  value       = local.usar_dominio ? aws_route53_zone.site[0].name_servers : []
}

output "cloudfront_distribution_id" {
  value = aws_cloudfront_distribution.this.id
}

output "site_bucket" {
  value = aws_s3_bucket.site.bucket
}

output "ecr_repository_url" {
  value = aws_ecr_repository.api.repository_url
}

output "instance_id" {
  value = aws_instance.app.id
}

output "db_endpoint" {
  value = aws_db_instance.this.address
}

output "deploy_role_arn" {
  description = "Role que o GitHub Actions assume (variável AWS_DEPLOY_ROLE_ARN). Vazio com github_deploy_enabled = false."
  value       = var.github_deploy_enabled ? aws_iam_role.deploy[0].arn : ""
}

output "region" {
  value = var.region
}
