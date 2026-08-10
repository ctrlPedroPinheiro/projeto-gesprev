package iff.edu.br.gesprev.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.List;
import java.util.Arrays;

/**
 * Configuração de segurança para a aplicação.
 * Define as regras de autenticação e autorização, incluindo o filtro JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    private static final String DIRETOR = "DIRETOR";
    private static final String ANALISTA = "ANALISTA";

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF já que usamos JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Define que a API não guarda estado (Stateless)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll()
                
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                .requestMatchers("/api/usuarios/**").hasRole(DIRETOR)

                .requestMatchers(HttpMethod.POST, "/api/processos-aposentadoria/com-servidor").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.POST, "/api/processos-aposentadoria/preprocessar-ficha-funcional").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.GET, "/api/processos-aposentadoria").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.GET, "/api/processos-aposentadoria/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.PUT, "/api/processos-aposentadoria/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.PATCH, "/api/processos-aposentadoria/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.DELETE, "/api/processos-aposentadoria/**").hasRole(DIRETOR)

                .requestMatchers(HttpMethod.POST, "/api/atos-aposentadoria/gerar").hasRole(DIRETOR)

                .requestMatchers(HttpMethod.GET, "/api/documentos/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.POST, "/api/documentos").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.POST, "/api/documentos/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.PUT, "/api/documentos/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.DELETE, "/api/documentos/**").hasRole(DIRETOR)

                .requestMatchers(HttpMethod.GET, "/api/checklist-documentos/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.POST, "/api/checklist-documentos").hasRole(DIRETOR)
                .requestMatchers("/api/checklist-documentos/**").hasRole(DIRETOR)

                .requestMatchers(HttpMethod.GET, "/api/historicos-processo/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.POST, "/api/historicos-processo").hasRole(DIRETOR)
                .requestMatchers("/api/historicos-processo/**").hasRole(DIRETOR)

                .requestMatchers(HttpMethod.POST, "/api/memorias-calculo/calcular/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.GET, "/api/memorias-calculo/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.DELETE, "/api/memorias-calculo/**").hasRole(DIRETOR)

                .requestMatchers(HttpMethod.GET, "/api/servidores").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.GET, "/api/servidores/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.PUT, "/api/servidores/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.POST, "/api/servidores").hasRole(DIRETOR)
                .requestMatchers("/api/servidores/**").hasRole(DIRETOR)

                .requestMatchers("/api/vlm/**").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers(HttpMethod.POST, "/api/chat").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers("/api/chat/**").hasAnyRole(ANALISTA, DIRETOR)

                .requestMatchers(HttpMethod.GET, "/api/fatores-atualizacao/verificar").hasAnyRole(ANALISTA, DIRETOR)
                .requestMatchers("/api/fatores-atualizacao/**").hasRole(DIRETOR)
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
