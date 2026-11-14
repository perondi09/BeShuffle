// CloudFront Function (viewer-request) do site: caminhos sem extensão são rotas do React.
function handler(event) {
  var request = event.request;
  if (request.uri.indexOf('.') === -1) {
    request.uri = '/index.html';
  }
  return request;
}
