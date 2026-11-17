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
| Senha | definida pela variável de ambiente `ADMIN_PASSWORD` (o usuário só é criado na primeira execução, e só se a variável existir) |

## Como rodar localmente

1. Clone o repositório
2. Certifique-se de ter o Java 21 e o Docker instalados
3. Suba o banco de dados:

```bash
docker compose up -d postgres
```

4. Defina as variáveis de ambiente. A configuração fica em `src/main/resources/application.properties` e só lê variáveis, sem nenhum segredo no código:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/deadline
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET="$(openssl rand -base64 48)"
export ADMIN_PASSWORD="escolha-uma-senha-forte"
```

5. Rode o projeto com Maven:

```bash
./mvnw spring-boot:run
```

Opcionalmente, `CORS_ALLOWED_ORIGINS` define de quais endereços a interface pode chamar a API (o padrão é `http://localhost:5173`).

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
