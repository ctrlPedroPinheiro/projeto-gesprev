package iff.edu.br.gesprev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GESPREV API")
                        .description("""
                                API do GESPREV para gestao do fluxo de aposentadoria.

                                Fluxo principal:
                                1. Analista cria o processo com os dados do servidor.
                                2. Sistema gera checklist e historico inicial.
                                3. Analista anexa documentos reais ao processo.
                                4. Analista valida os documentos processados.
                                5. Memoria de calculo e gerada apos todos os documentos obrigatorios estarem entregues e validados.
                                6. Diretor gera o Ato de Aposentadoria, finalizando o processo.

                                Use o botao Authorize com o token JWT retornado pelo login.
                                Endpoints de IA/VLM estao documentados como apoio futuro ao fluxo.
                                """)
                        .version("1.0.0"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Ambiente local padrao"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
