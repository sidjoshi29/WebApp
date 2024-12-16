package edu.ncsu.csc326.wolfcafe.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Checks user's tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Token provider */
    private final JwtTokenProvider   jwtTokenProvider;

    /** Service for UserDetails */
    private final UserDetailsService userDetailsService;

    /**
     * Constructs the authentication filter
     *
     * @param jwtTokenProvider
     *            token provide
     * @param userDetailsService
     *            service for UserDetails
     */
    public JwtAuthenticationFilter ( final JwtTokenProvider jwtTokenProvider,
            final UserDetailsService userDetailsService ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Loads the user given the token.
     *
     * @param request
     *            request from client
     * @param response
     *            response for the request
     * @param filterChain
     *            permissions
     */
    @Override
    protected void doFilterInternal ( final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain ) throws ServletException, IOException {
        // Get JWT token from HTTP request
        final String token = getTokenFromRequest( request );

        // Validate token
        if ( StringUtils.hasText( token ) && jwtTokenProvider.validateToken( token ) ) {
            // get username from token
            final String username = jwtTokenProvider.getUsername( token );

            final UserDetails userDetails = userDetailsService.loadUserByUsername( username );

            final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities() );

            authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );

            SecurityContextHolder.getContext().setAuthentication( authenticationToken );
        }

        filterChain.doFilter( request, response );
    }

    /**
     * Returns the Bearer token from the given request
     *
     * @param request
     *            the request that was just made
     * @return the bearer token associated with that request
     */
    private String getTokenFromRequest ( final HttpServletRequest request ) {
        final String bearerToken = request.getHeader( "Authorization" );
        if ( StringUtils.hasText( bearerToken ) && bearerToken.startsWith( "Bearer " ) ) {
            return bearerToken.substring( 7, bearerToken.length() );
        }

        return null;
    }
}
