# Dead Line Store - API REST  

![CI](https://github.com/ericpatricio23/dead-line-store-spring/actions/workflows/ci.yml/badge.svg)
API REST para controle de estoque e vendas de loja, desenvolvida com Spring Boot.

## Tecnologias

- Java 21
- Spring Boot 3.5.0
- Spring Security + JWT
- Spring Data JPA
- SQLite (desenvolvimento local)
- PostgreSQL (Docker/produção)
- H2 (testes)
- Lombok
- Swagger/Postman
- Docker

## Funcionalidades

- CRUD de Produtos
- Registro de Vendas com itens
- Autenticação JWT para o administrador
- Rotas públicas para funcionários
- Tratamento global de exceções
- Validação de DTOs
- Documentação via Swagger

## Segurança

- **Admin** → faz login em `/auth/login` e tem acesso total
- **Funcionário** → acessa sem autenticação, pode listar produtos e registrar vendas

### Credenciais padrão

| Campo | Valor |
|-------|-------|
| Usuário | `admin` |
| Senha | definida no código — altere antes de ir para produção |

## Como rodar localmente

1. Clone o repositório
2. Certifique-se de ter o Java 21 instalado
3. Copie o `application.example.properties` para `application.properties`
4. Preencha o `jwt.secret` com uma chave secreta
5. Rode o projeto com Maven:

```bash
./mvnw spring-boot:run
```

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

🐳 [Docker Hub](https://hub.docker.com/r/ericpatricio10/dead-line-store)

## Documentação

Após rodar, acesse o Swagger em:

```
http://localhost:8080/swagger-ui/index.html
```
