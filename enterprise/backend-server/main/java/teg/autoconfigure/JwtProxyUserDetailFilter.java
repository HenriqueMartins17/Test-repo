package com.apitable.enterprise.teg.autoconfigure;

import static com.apitable.auth.enums.AuthException.UNAUTHORIZED;

import cn.hutool.core.util.StrUtil;
import com.apitable.interfaces.social.facade.SocialServiceFacade;
import com.apitable.shared.component.ResourceDefinition;
import com.apitable.shared.component.scanner.ApiResourceFactory;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.ApiHelper;
import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * jwt proxy user detail filter.
 *
 * @author Shawn Deng
 */
@Slf4j
public class JwtProxyUserDetailFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtProxyUserDetailFilter.class);

    private final BeanFactory beanFactory;

    private final ApiResourceFactory apiResourceFactory;

    public JwtProxyUserDetailFilter(BeanFactory beanFactory,
                                    ApiResourceFactory apiResourceFactory) {
        this.beanFactory = beanFactory;
        this.apiResourceFactory = apiResourceFactory;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull final HttpServletResponse response,
                                    @NotNull final FilterChain filterChain)
        throws ServletException, IOException {
        String serverPath = request.getServletPath();
        ResourceDefinition resourceDef =
            apiResourceFactory.getResourceByUrl(serverPath, request.getMethod());
        String ignoreUrl =
            (String) request.getAttribute(
                TegProperties.SmartProxyHeaderProperty.REQUEST_IGNORE_URL);
        if (serverPath.equals(ignoreUrl)) {
            LOGGER.debug("Service check to Smart Proxy ignore path: {}", serverPath);
            filterChain.doFilter(request, response);
            return;
        }

        boolean isCheckUrl =
            (boolean) request.getAttribute(
                TegProperties.SmartProxyHeaderProperty.REQUEST_CHECK_URL);
        if (resourceDef != null) {
            if (!resourceDef.getRequiredLogin() && !isCheckUrl) {
                LOGGER.debug("No session path required: {}", serverPath);
                filterChain.doFilter(request, response);
                return;
            }
        }

        String apiKey = ApiHelper.getApiKey(request);
        if (StrUtil.isNotBlank(apiKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get Identity Single Sign On
        String jwtStaffName =
            request.getHeader(TegProperties.SmartProxyHeaderProperty.REQUEST_STAFFNAME);
        if (StrUtil.isBlank(jwtStaffName)) {
            throw new ServletException(UNAUTHORIZED.getMessage());
        }
        SocialServiceFacade socialServiceFacade = beanFactory.getBean(SocialServiceFacade.class);
        Long userId = socialServiceFacade.getUserIdByUnionId(jwtStaffName);
        if (userId == null) {
            // Unsynced users are not allowed to log in
            log.info("User does not exist, please create user first[{}]", jwtStaffName);
            throw new ServletException(UNAUTHORIZED.getMessage());
        }
        SessionContext.setExternalId(userId, jwtStaffName);
        filterChain.doFilter(request, response);
    }
}
