package com.gestordeordenes.gestorordenes.config; // O tu paquete

import com.gestordeordenes.gestorordenes.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final AuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsServiceImpl,
                          AuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso a recursos estáticos
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/vendor/**",
                                "/favicon.ico"
                        ).permitAll()
                        // Permitir acceso a la página de login y al proceso de login
                        .requestMatchers("/login", "/perform_login").permitAll()
                        // Permitir acceso a las rutas públicas para consulta de órdenes y la página de error
                        .requestMatchers(
                                "/",
                                "/consultar-orden",
                                "/orden/buscar",
                                "/error"
                        ).permitAll()
                        // --- AÑADIR ESTAS LÍNEAS PARA SWAGGER ---
                        .requestMatchers(
                                "/swagger-ui.html",    // La página principal de Swagger UI
                                "/swagger-ui/**",      // Todos los recursos de Swagger UI (CSS, JS, etc.)
                                "/v3/api-docs/**",     // La especificación OpenAPI JSON/YAML
                                "/swagger-resources/**",// Recursos adicionales de Swagger (a veces necesario)
                                "/webjars/**"          // Webjars que Swagger UI podría usar
                        ).permitAll()
                        // --- FIN LÍNEAS PARA SWAGGER ---
                        // Rutas para TECNICOS
                        .requestMatchers("/tecnico/**").hasRole("TECNICO")
                        // Rutas para ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Todas las demás peticiones requieren autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(userDetailsServiceImpl);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsServiceImpl)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
