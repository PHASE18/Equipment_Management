backend/
├── pom.xml                          # Spring Boot 3.2 + MyBatis Plus + JWT + EasyExcel + MinIO
├── src/main/resources/application.yml
└── src/main/java/com/equipment/management/
    ├── common/                      # 统一基础设施
    │   ├── result/Result.java       # { code, message, data }
    │   ├── result/PageResult.java   # { records, total, pageNum, pageSize, pages }
    │   ├── query/PageQuery.java     # pageNum/pageSize/keyword/sortField/sortOrder
    │   ├── constant/ErrorCode.java  # 200/401/1001~1015 错误码
    │   ├── exception/               # BusinessException + GlobalExceptionHandler
    │   ├── context/UserContext.java # 当前登录用户 ThreadLocal
    │   └── util/JwtUtils.java
    ├── interceptor/JwtInterceptor.java   # Bearer Token 统一鉴权
    ├── config/WebMvcConfig.java          # 拦截 /api/**，白名单 /api/login
    ├── controller/                  # 15 个 Controller
    ├── service/ + service/impl/     # 15 个 Service 接口 + 实现（TODO 占位）
    ├── dto/request/ + dto/response/
    └── entity/                      # 与数据库表对应的实体