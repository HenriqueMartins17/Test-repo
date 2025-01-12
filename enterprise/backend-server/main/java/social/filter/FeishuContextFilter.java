package com.apitable.enterprise.social.filter;

import static com.apitable.shared.constants.FilterConstants.FIRST_ORDERED;

import com.vikadata.social.feishu.FeishuConfigStorageHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

public class FeishuContextFilter extends OncePerRequestFilter implements Ordered {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            FeishuConfigStorageHolder.remove();
        }
    }

    @Override
    public int getOrder() {
        return FIRST_ORDERED + 20;
    }
}
