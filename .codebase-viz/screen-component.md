# Screen–Component Mapping

```mermaid
%% chunk:1/7
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#0c1a30','primaryTextColor':'#7dd3fc','primaryBorderColor':'#0e3a6e','edgeLabelBackground':'#0c1a30','lineColor':'#334155','secondaryColor':'#0f172a','clusterBkg':'#060c18','clusterBorder':'#1e3a5f','fontFamily':'JetBrains Mono','fontSize':'14'},'flowchart':{'nodeSpacing':25,'rankSpacing':8,'padding':4}}}%%
graph TD
  classDef ssr fill:#0d1a0d,stroke:#16a34a,color:#86efac
  classDef ctrl fill:#042f2e,stroke:#0d9488,color:#5eead4
  classDef csr fill:#2d1200,stroke:#c2410c,color:#fb923c
  classDef ssg fill:#1a0d1a,stroke:#7c3aed,color:#c4b5fd
  classDef isr fill:#1a1a0d,stroke:#ca8a04,color:#fde047
  classDef ppr fill:#0d1a2d,stroke:#2563eb,color:#93c5fd
  classDef unk fill:#1a1a1a,stroke:#6b7280,color:#9ca3af
  classDef pkg fill:#0c1018,stroke:#475569,color:#cbd5e1
  classDef ext fill:#2d1a06,stroke:#d97706,color:#fcd34d
  classDef muted fill:#0a0d14,stroke:#374151,color:#64748b,stroke-dasharray: 3 3
  classDef hdr fill:#06080f,stroke:#1e3a5f,color:#7dd3fc
  subgraph HDR_PKG ["📁 src/main/java/com.CompraVenta.Backend.Modules.Articles"]
    direction TB
  subgraph di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController["[ DI ]"]
    direction TB
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController["ArticleController"]:::ctrl
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Service_ArticleService_java_ArticleService["ArticleService"]:::unk
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController -.-> di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Service_ArticleService_java_ArticleService
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController__ext0["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Service_ArticleService_java_ArticleService -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Articles_Controller_ArticleController_java_ArticleController__ext0
  end
  end
%%--CHUNK--%%
%% chunk:2/7
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#0c1a30','primaryTextColor':'#7dd3fc','primaryBorderColor':'#0e3a6e','edgeLabelBackground':'#0c1a30','lineColor':'#334155','secondaryColor':'#0f172a','clusterBkg':'#060c18','clusterBorder':'#1e3a5f','fontFamily':'JetBrains Mono','fontSize':'14'},'flowchart':{'nodeSpacing':25,'rankSpacing':8,'padding':4}}}%%
graph TD
  classDef ssr fill:#0d1a0d,stroke:#16a34a,color:#86efac
  classDef ctrl fill:#042f2e,stroke:#0d9488,color:#5eead4
  classDef csr fill:#2d1200,stroke:#c2410c,color:#fb923c
  classDef ssg fill:#1a0d1a,stroke:#7c3aed,color:#c4b5fd
  classDef isr fill:#1a1a0d,stroke:#ca8a04,color:#fde047
  classDef ppr fill:#0d1a2d,stroke:#2563eb,color:#93c5fd
  classDef unk fill:#1a1a1a,stroke:#6b7280,color:#9ca3af
  classDef pkg fill:#0c1018,stroke:#475569,color:#cbd5e1
  classDef ext fill:#2d1a06,stroke:#d97706,color:#fcd34d
  classDef muted fill:#0a0d14,stroke:#374151,color:#64748b,stroke-dasharray: 3 3
  classDef hdr fill:#06080f,stroke:#1e3a5f,color:#7dd3fc
  subgraph HDR_PKG ["📁 src/main/java/com.CompraVenta.Backend.Modules.auth"]
    direction TB
  subgraph di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController["[ DI ]"]
    direction TB
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController["AuthController"]:::ctrl
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Service_AuthService_java_AuthService["AuthService"]:::unk
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController -.-> di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Service_AuthService_java_AuthService
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__ext0["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Service_AuthService_java_AuthService -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__ext0
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__ext1["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_auth_Controller_AuthController_java_AuthController__ext1
  end
  end
%%--CHUNK--%%
%% chunk:3/7
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#0c1a30','primaryTextColor':'#7dd3fc','primaryBorderColor':'#0e3a6e','edgeLabelBackground':'#0c1a30','lineColor':'#334155','secondaryColor':'#0f172a','clusterBkg':'#060c18','clusterBorder':'#1e3a5f','fontFamily':'JetBrains Mono','fontSize':'14'},'flowchart':{'nodeSpacing':25,'rankSpacing':8,'padding':4}}}%%
graph TD
  classDef ssr fill:#0d1a0d,stroke:#16a34a,color:#86efac
  classDef ctrl fill:#042f2e,stroke:#0d9488,color:#5eead4
  classDef csr fill:#2d1200,stroke:#c2410c,color:#fb923c
  classDef ssg fill:#1a0d1a,stroke:#7c3aed,color:#c4b5fd
  classDef isr fill:#1a1a0d,stroke:#ca8a04,color:#fde047
  classDef ppr fill:#0d1a2d,stroke:#2563eb,color:#93c5fd
  classDef unk fill:#1a1a1a,stroke:#6b7280,color:#9ca3af
  classDef pkg fill:#0c1018,stroke:#475569,color:#cbd5e1
  classDef ext fill:#2d1a06,stroke:#d97706,color:#fcd34d
  classDef muted fill:#0a0d14,stroke:#374151,color:#64748b,stroke-dasharray: 3 3
  classDef hdr fill:#06080f,stroke:#1e3a5f,color:#7dd3fc
  subgraph HDR_PKG ["📁 src/main/java/com.CompraVenta.Backend.Modules.Clients"]
    direction TB
  subgraph di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController["[ DI ]"]
    direction TB
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController["ClienteController"]:::ctrl
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Service_ClienteService_java_ClienteService["ClienteService"]:::unk
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController -.-> di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Service_ClienteService_java_ClienteService
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController__ext0["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Service_ClienteService_java_ClienteService -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Clients_Controller_ClienteController_java_ClienteController__ext0
  end
  end
%%--CHUNK--%%
%% chunk:4/7
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#0c1a30','primaryTextColor':'#7dd3fc','primaryBorderColor':'#0e3a6e','edgeLabelBackground':'#0c1a30','lineColor':'#334155','secondaryColor':'#0f172a','clusterBkg':'#060c18','clusterBorder':'#1e3a5f','fontFamily':'JetBrains Mono','fontSize':'14'},'flowchart':{'nodeSpacing':25,'rankSpacing':8,'padding':4}}}%%
graph TD
  classDef ssr fill:#0d1a0d,stroke:#16a34a,color:#86efac
  classDef ctrl fill:#042f2e,stroke:#0d9488,color:#5eead4
  classDef csr fill:#2d1200,stroke:#c2410c,color:#fb923c
  classDef ssg fill:#1a0d1a,stroke:#7c3aed,color:#c4b5fd
  classDef isr fill:#1a1a0d,stroke:#ca8a04,color:#fde047
  classDef ppr fill:#0d1a2d,stroke:#2563eb,color:#93c5fd
  classDef unk fill:#1a1a1a,stroke:#6b7280,color:#9ca3af
  classDef pkg fill:#0c1018,stroke:#475569,color:#cbd5e1
  classDef ext fill:#2d1a06,stroke:#d97706,color:#fcd34d
  classDef muted fill:#0a0d14,stroke:#374151,color:#64748b,stroke-dasharray: 3 3
  classDef hdr fill:#06080f,stroke:#1e3a5f,color:#7dd3fc
  subgraph HDR_PKG ["📁 src/main/java/com.CompraVenta.Backend.Modules.Employee"]
    direction TB
  subgraph di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController["[ DI ]"]
    direction TB
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController["EmployeeController"]:::ctrl
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Service_EmployeeService_java_EmployeeService["EmployeeService"]:::unk
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController -.-> di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Service_EmployeeService_java_EmployeeService
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController__ext0["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Service_EmployeeService_java_EmployeeService -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Employee_Controller_EmployeeController_java_EmployeeController__ext0
  end
  end
%%--CHUNK--%%
%% chunk:5/7
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#0c1a30','primaryTextColor':'#7dd3fc','primaryBorderColor':'#0e3a6e','edgeLabelBackground':'#0c1a30','lineColor':'#334155','secondaryColor':'#0f172a','clusterBkg':'#060c18','clusterBorder':'#1e3a5f','fontFamily':'JetBrains Mono','fontSize':'14'},'flowchart':{'nodeSpacing':25,'rankSpacing':8,'padding':4}}}%%
graph TD
  classDef ssr fill:#0d1a0d,stroke:#16a34a,color:#86efac
  classDef ctrl fill:#042f2e,stroke:#0d9488,color:#5eead4
  classDef csr fill:#2d1200,stroke:#c2410c,color:#fb923c
  classDef ssg fill:#1a0d1a,stroke:#7c3aed,color:#c4b5fd
  classDef isr fill:#1a1a0d,stroke:#ca8a04,color:#fde047
  classDef ppr fill:#0d1a2d,stroke:#2563eb,color:#93c5fd
  classDef unk fill:#1a1a1a,stroke:#6b7280,color:#9ca3af
  classDef pkg fill:#0c1018,stroke:#475569,color:#cbd5e1
  classDef ext fill:#2d1a06,stroke:#d97706,color:#fcd34d
  classDef muted fill:#0a0d14,stroke:#374151,color:#64748b,stroke-dasharray: 3 3
  classDef hdr fill:#06080f,stroke:#1e3a5f,color:#7dd3fc
  subgraph HDR_PKG ["📁 src/main/java/com.CompraVenta.Backend.Modules.Pawns"]
    direction TB
  subgraph di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController["[ DI ]"]
    direction TB
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController["PawnController"]:::ctrl
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Service_PawnService_java_PawnService["PawnService"]:::unk
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController -.-> di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Service_PawnService_java_PawnService
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController__ext0["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Service_PawnService_java_PawnService -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Pawns_Controller_PawnController_java_PawnController__ext0
  end
  end
%%--CHUNK--%%
%% chunk:6/7
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#0c1a30','primaryTextColor':'#7dd3fc','primaryBorderColor':'#0e3a6e','edgeLabelBackground':'#0c1a30','lineColor':'#334155','secondaryColor':'#0f172a','clusterBkg':'#060c18','clusterBorder':'#1e3a5f','fontFamily':'JetBrains Mono','fontSize':'14'},'flowchart':{'nodeSpacing':25,'rankSpacing':8,'padding':4}}}%%
graph TD
  classDef ssr fill:#0d1a0d,stroke:#16a34a,color:#86efac
  classDef ctrl fill:#042f2e,stroke:#0d9488,color:#5eead4
  classDef csr fill:#2d1200,stroke:#c2410c,color:#fb923c
  classDef ssg fill:#1a0d1a,stroke:#7c3aed,color:#c4b5fd
  classDef isr fill:#1a1a0d,stroke:#ca8a04,color:#fde047
  classDef ppr fill:#0d1a2d,stroke:#2563eb,color:#93c5fd
  classDef unk fill:#1a1a1a,stroke:#6b7280,color:#9ca3af
  classDef pkg fill:#0c1018,stroke:#475569,color:#cbd5e1
  classDef ext fill:#2d1a06,stroke:#d97706,color:#fcd34d
  classDef muted fill:#0a0d14,stroke:#374151,color:#64748b,stroke-dasharray: 3 3
  classDef hdr fill:#06080f,stroke:#1e3a5f,color:#7dd3fc
  subgraph HDR_PKG ["📁 src/main/java/com.CompraVenta.Backend.Modules.Purchases"]
    direction TB
  subgraph di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController["[ DI ]"]
    direction TB
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController["PurchaseController"]:::ctrl
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Service_PurchaseService_java_PurchaseService["PurchaseService"]:::unk
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController -.-> di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Service_PurchaseService_java_PurchaseService
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController__ext0["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Service_PurchaseService_java_PurchaseService -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Purchases_Controller_PurchaseController_java_PurchaseController__ext0
  end
  end
%%--CHUNK--%%
%% chunk:7/7
%%{init:{'theme':'base','themeVariables':{'background':'#060810','primaryColor':'#0c1a30','primaryTextColor':'#7dd3fc','primaryBorderColor':'#0e3a6e','edgeLabelBackground':'#0c1a30','lineColor':'#334155','secondaryColor':'#0f172a','clusterBkg':'#060c18','clusterBorder':'#1e3a5f','fontFamily':'JetBrains Mono','fontSize':'14'},'flowchart':{'nodeSpacing':25,'rankSpacing':8,'padding':4}}}%%
graph TD
  classDef ssr fill:#0d1a0d,stroke:#16a34a,color:#86efac
  classDef ctrl fill:#042f2e,stroke:#0d9488,color:#5eead4
  classDef csr fill:#2d1200,stroke:#c2410c,color:#fb923c
  classDef ssg fill:#1a0d1a,stroke:#7c3aed,color:#c4b5fd
  classDef isr fill:#1a1a0d,stroke:#ca8a04,color:#fde047
  classDef ppr fill:#0d1a2d,stroke:#2563eb,color:#93c5fd
  classDef unk fill:#1a1a1a,stroke:#6b7280,color:#9ca3af
  classDef pkg fill:#0c1018,stroke:#475569,color:#cbd5e1
  classDef ext fill:#2d1a06,stroke:#d97706,color:#fcd34d
  classDef muted fill:#0a0d14,stroke:#374151,color:#64748b,stroke-dasharray: 3 3
  classDef hdr fill:#06080f,stroke:#1e3a5f,color:#7dd3fc
  subgraph HDR_PKG ["📁 src/main/java/com.CompraVenta.Backend.Modules.Sale"]
    direction TB
  subgraph di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController["[ DI ]"]
    direction TB
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController["SaleController"]:::ctrl
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Service_SaleService_java_SaleService["SaleService"]:::unk
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController -.-> di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Service_SaleService_java_SaleService
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController__ext0["(external Service)"]:::muted
    di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController__component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Service_SaleService_java_SaleService -.->|"cross-pkg"| di_component_Backend_src_main_java_com_CompraVenta_Backend_Modules_Sale_Controller_SaleController_java_SaleController__ext0
  end
  end
```
