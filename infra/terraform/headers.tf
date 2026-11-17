# Cabeçalhos de segurança do site. Substitui a política gerenciada "Managed-SecurityHeadersPolicy", que não
# traz Content-Security-Policy nem Permissions-Policy. Fontes vêm do Google Fonts e as capas dos discos do CDN
# do Spotify (scdn.co); o React usa style inline, por isso 'unsafe-inline' só em style-src.
resource "aws_cloudfront_response_headers_policy" "site" {
  name    = "${var.project}-security-headers"
  comment = "HSTS, CSP, anti-clickjacking e demais cabecalhos de seguranca"

  security_headers_config {
    strict_transport_security {
      access_control_max_age_sec = 31536000
      include_subdomains         = true
      preload                    = false
      override                   = true
    }
    content_type_options {
      override = true
    }
    frame_options {
      frame_option = "DENY"
      override     = true
    }
    referrer_policy {
      referrer_policy = "strict-origin-when-cross-origin"
      override        = true
    }
    content_security_policy {
      content_security_policy = join("; ", [
        "default-src 'self'",
        "script-src 'self'",
        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
        "font-src 'self' https://fonts.gstatic.com",
        "img-src 'self' data: https://*.scdn.co",
        "connect-src 'self'",
        "object-src 'none'",
        "base-uri 'self'",
        "form-action 'self'",
        "frame-ancestors 'none'",
      ])
      override = true
    }
  }

  custom_headers_config {
    items {
      header   = "Permissions-Policy"
      value    = "camera=(), microphone=(), geolocation=(), payment=(), usb=()"
      override = true
    }
  }
}
