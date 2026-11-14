// CloudFront Function (viewer-request) do site.
// 1. www.<domínio> redireciona para o domínio sem www: um endereço só, e o cookie de login vale em um lugar só.
// 2. Caminhos sem extensão são rotas do React e recebem o index.html.
function handler(event) {
  var request = event.request;
  var host = request.headers.host ? request.headers.host.value : '';

  if (host.indexOf('www.') === 0) {
    var partes = [];
    for (var chave in request.querystring) {
      var item = request.querystring[chave];
      if (item.multiValue) {
        for (var i = 0; i < item.multiValue.length; i++) {
          partes.push(chave + '=' + item.multiValue[i].value);
        }
      } else {
        partes.push(item.value === '' ? chave : chave + '=' + item.value);
      }
    }
    var destino = 'https://' + host.substring(4) + request.uri + (partes.length ? '?' + partes.join('&') : '');
    return {
      statusCode: 301,
      statusDescription: 'Moved Permanently',
      headers: { location: { value: destino } }
    };
  }

  if (request.uri.indexOf('.') === -1) {
    request.uri = '/index.html';
  }
  return request;
}
