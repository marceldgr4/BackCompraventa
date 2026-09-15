# Data Flow (Screen ↔ Data Source)

```mermaid
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#2a4055','primaryTextColor':'#f8fafc','primaryBorderColor':'#1e4060','lineColor':'#f59e0b','secondaryColor':'#0f172a','tertiaryColor':'#1a0a20','attributeBackgroundColorEven':'#ffffff','attributeBackgroundColorOdd':'#f1f5f9','textColor':'#1e293b','nodeBorder':'#1e4060','clusterBkg':'#0a0e1a','fontFamily':'JetBrains Mono','fontSize':'14'}}}%%
erDiagram
%% table:audit_log path:Backend/src/main/java/com/CompraVenta/Backend/Audit/entity/AudLog.java
%% table:articles path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Articles/Entity/Article.java
%% table:clientes path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Clients/Entity/Cliente.java
%% table:employees path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Employee/Entity/Employee.java
%% table:pawns path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Pawns/Entity/Pawn.java
%% table:pawn_paymnts path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Pawns/Entity/PawnPayment.java
%% table:purchases path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Purchases/Entity/Purchase.java
%% table:sales path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Sale/Entity/Sale.java
%% table:sales_details path:Backend/src/main/java/com/CompraVenta/Backend/Modules/Sale/Entity/SaleDetails.java
%% table:sync_outbox path:Backend/src/main/java/com/CompraVenta/Backend/Sync/SyncOutbox.java
  audit_log {
    Long id PK
    String employee_id
    String operation
    String entity_type
    String entity_id
    String before_value
    String after_value
    String error_message
    String ip_address
    Instant timestamp
  }
  articles {
    Long cliente_id
    String name_article
    String description
    ArticleCategory category
    SourceType source_Type
    ItemStatus item_State
    Integer amount
    BigDecimal price
    BigDecimal purchase_Price
  }
  clientes {
    String cedula
    String first_name
    String last_name
    String email
    String phone
    String address
    String city
    ClienteStatus status
    RegistrationType registration_type
  }
  employees {
    String email
    String full_name
    String password_hash
    Role rol
    boolean active
  }
  pawns {
    Long employee_id
    Article article_id FK
    Long article_id
    Cliente cliente_id FK
    Long cliente_id
    Integer amount
    BigDecimal price
    BigDecimal weight_grams
    Integer installment_count
    Integer installments_paid
    Integer installments_missed
    LocalDate pawn_date
    LocalDate return_date
    PawnStatus status
    String notes
  }
  pawn_paymnts {
    Long id PK
    Pawn pawn_id FK
    Long pawn_id
    BigDecimal amount
    LocalDate payment_date
    String notes
    Long created_by_employee_id
    Boolean is_missed
    Instant created_at
  }
  purchases {
    Long id PK
    UUID global_id
    Employee employee_id FK
    Long employee_id
    Cliente cliente_id FK
    Long cliente_id
    Article article_id FK
    Long article_id
    BigDecimal purchase_price
    LocalDateTime purchase_date
    String notes
    LocalDateTime created_at
  }
  sales {
    Employee employee_id FK
    Long employee_id
    Cliente cliente_id FK
    Long cliente_id
    String cliente_nombre_anon
    LocalDate sale_date
    String notes
  }
  sales_details {
    Sale sale_id FK
    Long sale_id
    Article article_id FK
    Long article_id
    Integer amount
    BigDecimal unit_price
  }
  sync_outbox {
    Long id PK
    String entity_type
    UUID entity_id
    String operation
    String payload
    long local_version
    long cloud_version
    SyncStatus status
    int retry_count
    String error_message
    Instant created_at
    Instant synced_at
  }
  AuditRepository {
    string name
  }
  ArticleMapper {
    string name
  }
  ArticleRepository {
    string name
  }
  ClienteMapper {
    string name
  }
  ClienteRepository {
    string name
  }
  EmployeeMapper {
    string name
  }
  EmployeeRepository {
    string name
  }
  PawnMapper {
    string name
  }
  PawnPaymentRepository {
    string name
  }
  PawnRepository {
    string name
  }
  PurchaseMapper {
    string name
  }
  PurchaseRepository {
    string name
  }
  SaleMapper {
    string name
  }
  SaleDetailRepository {
    string name
  }
  SaleProcedureRepository {
    string name
  }
  SaleRepository {
    string name
  }
  pawns }o--|| articles : "article_id"
  pawns }o--|| clientes : "cliente_id"
  pawn_paymnts }o--|| pawns : "pawn_id"
  purchases }o--|| employees : "employee_id"
  purchases }o--|| clientes : "cliente_id"
  purchases }o--|| articles : "article_id"
  sales }o--|| employees : "employee_id"
  sales }o--|| clientes : "cliente_id"
  sales_details }o--|| sales : "sale_id"
  sales_details }o--|| articles : "article_id"
```
