import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RateLimitingFilterTest {
    private RateLimitingFilter filter;
    private RateLimiter rateLimiter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @Before
    public void setUp() {
        rateLimiter = new RateLimiter();
        filter = new RateLimitingFilter(rateLimiter);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    public void allowsRequestsUnderLimit() throws Exception {
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
    }

    @Test
    public void returnsTooManyRequestsWhenExceeded() throws Exception {
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_TOO_MANY_REQUESTS, response.getStatus());
    }

    @Test
    public void usesXForwardedForHeader() throws Exception {
        request.addHeader("X-Forwarded-For", "203.0.113.1, 192.168.1.1");
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_TOO_MANY_REQUESTS, response.getStatus());
    }
}
