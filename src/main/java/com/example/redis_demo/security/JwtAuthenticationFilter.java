package com.example.redis_demo.security;

import com.example.redis_demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Lấy Header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Kiểm tra header có đúng định dạng "Bearer <token>" không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Không có token thì cho qua, để các filter sau chặn
            return;
        }

        // 3. Tách lấy token (chúng ta sẽ xử lý giải mã ở đây sau)
        String jwt = authHeader.substring(7);

        String username = jwtService.extractUsername(jwt); // Bạn cần viết hàm này trong JwtService

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

            // 1. Tạo đối tượng Authentication (đây là trạng thái "đã đăng nhập")
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null, // Không cần mật khẩu vì token đã xác thực rồi
                    authorities // Ở đây bạn có thể thêm Role/Quyền nếu muốn
            );

            // 2. Gán thông tin chi tiết của Request vào Authentication
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 3. Đưa Authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("Đã xác thực thành công cho user: " + username);
        }

        filterChain.doFilter(request, response);
    }
}