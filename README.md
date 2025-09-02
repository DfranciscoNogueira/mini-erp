# Mini-ERP

Este projeto é uma API **Spring Boot 3.5.x (Java 21)** para um mini-ERP com **Clientes**, **Produtos** e **Pedidos**, incluindo:
- Persistência (PostgreSQL)
- Validações (Jakarta Validation)
- Integração com **ViaCEP** (OpenFeign)
- **Swagger/OpenAPI** com `springdoc-openapi`
- **Testes** unitários (JUnit 5, Mockito, MockMvc)
- Execução via **Docker** e **Docker Compose**

---

## Arquitetura e versões

- **Java:** 21
- **Spring Boot:** 3.5.5
- **Spring Cloud:** 2025.0.0 (Northfields) — _compatível com Boot 3.5.x_
- **Springdoc (Swagger):** 2.8.x
- **Banco:** PostgreSQL (14+)
- **Build:** Maven 3.9+
- **Testes:** JUnit 5, Mockito (+ `spring-boot-test-mockito`)


## Pré-requisitos

- **Docker** e **Docker Compose** instalados
- **JDK 21**
- **Maven** 3.9+


## Como rodar (rápido) com Docker Compose

1. Na raiz do projeto, onde esta o `docker-compose.yml` e `Dockerfile` use o comando:
```bash
docker compose up --build
```

2. A aplicação deve iniciar em:  
   `http://localhost:8080`


## Build local (sem Docker)

1. Ajuste o `application.yml` conforme seu ambiente local.
2. Compile o projeto:
```bash
./mvnw clean package
# ou
mvn clean package
```
3. Rode:
```bash
java -jar target/*-SNAPSHOT.jar
```

## Swagger / OpenAPI

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON (default):** `http://localhost:8080/v3/api-docs`
- **OpenAPI do grupo (se configurado):** `http://localhost:8080/v3/api-docs/mini-erp-v1`


## cURLs de teste

### Clientes

**Criar cliente**
```bash
curl -X POST http://localhost:8080/api/v1/customers   -H "Content-Type: application/json"   -d '{
        "nome": "João Silva",
        "email": "joao@example.com",
        "cpf": "12345678900",
        "endereco": {
          "cep": "01001-000",
          "numero": "100"
        }
      }'
```

**Buscar cliente por ID**
```bash
curl http://localhost:8080/api/v1/customers/1
```

**Atualizar cliente**
```bash
curl -X PUT http://localhost:8080/api/v1/customers/1   -H "Content-Type: application/json"   -d '{
        "nome": "João Silva Jr",
        "email": "joaojr@example.com",
        "cpf": "12345678900",
        "endereco": {
          "cep": "01001-000",
          "numero": "200"
        }
      }'
```

**Listar clientes com filtro/paginação**
```bash
curl "http://localhost:8080/api/v1/customers?q=joao&page=0&size=10"
```

**Remover cliente**
```bash
curl -X DELETE http://localhost:8080/api/v1/customers/1
```

---

### Produtos

**Criar produto**
```bash
curl -X POST http://localhost:8080/api/v1/products   -H "Content-Type: application/json"   -d '{
        "sku": "SKU-001",
        "nome": "Camiseta",
        "precoBruto": 59.90,
        "estoque": 50,
        "estoqueMinimo": 10,
        "ativo": true
      }'
```

**Buscar produto por ID**
```bash
curl http://localhost:8080/api/v1/products/1
```

**Atualizar produto**
```bash
curl -X PUT http://localhost:8080/api/v1/products/1   -H "Content-Type: application/json"   -d '{
        "sku": "SKU-001",
        "nome": "Camiseta Premium",
        "precoBruto": 79.90,
        "estoque": 40,
        "estoqueMinimo": 5,
        "ativo": true
      }'
```

**Listar produtos (ativos)**
```bash
curl "http://localhost:8080/api/v1/products?ativo=true"
```

**Remover produto**
```bash
curl -X DELETE http://localhost:8080/api/v1/products/1
```

---

### Pedidos

**Criar pedido**
```bash
curl -X POST http://localhost:8080/api/v1/orders   -H "Content-Type: application/json"   -d '{
        "clienteId": 1,
        "itens": [
          { "produtoId": 1, "quantidade": 2, "desconto": 5.00 }
        ]
      }'
```

**Buscar pedido por ID**
```bash
curl http://localhost:8080/api/v1/orders/1
```

**Listar pedidos por status**
```bash
curl "http://localhost:8080/api/v1/orders?status=CRIADO"
```

**Pagar pedido**
```bash
curl -X POST http://localhost:8080/api/v1/orders/1/pay
```

**Cancelar pedido**
```bash
curl -X POST http://localhost:8080/api/v1/orders/1/cancel
```


## Testes automatizados

- **Unitários de service:** `PedidoServiceImplTest`, `ClienteServiceImplTest`, `ProdutoServiceImplTest`
- **Web layer (MockMvc) controllers:** `ClienteControllerTest`, `PedidoControllerTest`, `ProdutoControllerTest` (usando `@WebMvcTest` + `@MockitoBean`)

Rodando testes:
```bash
mvn test
# ou um teste específico
mvn -Dtest=ProdutoControllerTest test
```
