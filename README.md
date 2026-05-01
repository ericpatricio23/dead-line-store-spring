# Dead Line Store - API REST
API REST para controle de estoque e vendas de loja, desenvolvida com Spring Boot.

## Tecnologias
* Java 21
* Spring Boot 4.0.5
* Spring Security + JWT
* Spring Data JPA
* SQLite
* Lombok
* Swagger/OpenAPI
* Docker

## Funcionalidades
* CRUD de Produtos
* Registro de Vendas com itens
* Autenticação JWT para o administrador
* Rotas públicas para funcionários
* Tratamento global de exceções
* Validação de DTOs
* Documentação via Swagger

## Segurança
* Admin → faz login em `/auth/login` e tem acesso total
* Funcionário → acessa sem autenticação, pode listar produtos e registrar vendas

## Como rodar localmente
1. Clone o repositório
2. Copie o `application.example.properties` para `application.properties`
3. Preencha o `jwt.secret` com uma chave secreta
4. Rode o projeto com Maven

## Como rodar com Docker
1. Clone o repositório
2. Certifique-se de ter o Docker Desktop instalado e rodando
3. Suba a aplicação com:
```bash
docker compose up --build
```

Ou baixe a imagem direto do Docker Hub sem precisar clonar o repositório:
```bash
docker run -p 8080:8080 ericpatricio10/dead-line-store:latest
```

## Documentação
Após rodar, acesse o Swagger em:
```
http://localhost:8080/swagger-ui/index.html
```