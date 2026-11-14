resource "aws_instance" "app" {
  ami                    = data.aws_ssm_parameter.al2023_arm.value
  instance_type          = var.instance_type
  subnet_id              = sort(data.aws_subnets.app.ids)[0]
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = aws_iam_instance_profile.app.name

  # "standard": a instância nunca gasta além dos créditos de CPU (sem cobrança surpresa do modo unlimited).
  credit_specification {
    cpu_credits = "standard"
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    deploy_conf_b64 = base64encode(templatefile("${path.module}/deploy.conf.tftpl", {
      region         = var.region
      ecr_repository = aws_ecr_repository.api.repository_url
      db_host        = aws_db_instance.this.address
      db_name        = aws_db_instance.this.db_name
      db_secret_arn  = aws_db_instance.this.master_user_secret[0].secret_arn
      ssm_prefix     = "/${var.project}"
      log_group      = aws_cloudwatch_log_group.api.name
    }))
    deploy_sh_b64 = filebase64("${path.module}/deploy.sh")
  })

  lifecycle {
    # Uma AMI nova não recria o servidor; atualizações do sistema vêm pelo dnf.
    ignore_changes = [ami, user_data]
  }

  tags = {
    Name = "${var.project}-api"
  }
}

# IP fixo: o CloudFront aponta para o DNS público dele, que não muda se o EC2 reiniciar.
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  tags = {
    Name = "${var.project}-api"
  }
}
