@Component
public class RateLimitingFilter implements Filter {
  private static final String RATE_LIMIT_EXCEEDED_MSG = "Rate limit exceeded: max 10 requests per minute";
  private static final int TOO_MANY_REQUESTS_STATUS = 429;

  private final RateLimiter rateLimiter;

  @Autowired
  public RateLimitingFilter(RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String clientId = extractClientId(httpRequest);

    if (!rateLimiter.allowRequest(clientId)) {
      sendRateLimitResponse((HttpServletResponse) response);
      return;
    }

    chain.doFilter(request, response);
  }

  private String extractClientId(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isEmpty()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
    response.setStatus(TOO_MANY_REQUESTS_STATUS);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + RATE_LIMIT_EXCEEDED_MSG + "\"}");
  }

  @Override
  public void init(FilterConfig config) {}

  @Override
  public void destroy() {}
}
