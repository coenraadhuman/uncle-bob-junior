class HttpResponse {
  final int statusCode;
  final String body;

  HttpResponse(int statusCode, String body) {
    this.statusCode = statusCode;
    this.body = body;
  }
}
