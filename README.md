# 固定资产管理系统 (Fixed Asset Management System)

## 📖 项目简介
本项目是一个基于 Java Spring Boot 开发的轻量级固定资产管理系统。旨在帮助企业或组织高效地管理实物资产，提供从资产入库、变更、盘点到报废的全生命周期管理功能。系统界面简洁现代，操作直观，支持 Excel 批量导入导出及资产盘点差异自动对比。

## ✨ 主要功能

### 1. 资产管理
*   **资产全览**：列表展示所有资产，支持按关键字、类别、状态、部门、位置等多维度组合筛选。
*   **新增/编辑**：支持手动录入和修改资产详细信息。
*   **批量导入**：支持上传 Excel 文件批量导入资产数据，智能解析表头。
*   **资产转移**：支持资产在不同位置、不同管理员之间的流转，自动记录转移历史。
*   **批量删除**：支持多选资产进行批量删除。
*   **状态可视化**：不同资产状态（如正常、闲置、报废、丢失）使用不同颜色高亮显示，一目了然。

### 2. 盘点与审计
*   **资产快照**：可随时创建当前资产状态的快照（Snapshot），支持按年份筛选。
*   **差异对比**：选择两个不同时间点的快照进行对比，系统自动计算出“新增”、“丢失/报废”等差异项。
*   **报告导出**：支持将盘点对比结果导出为 Excel 报告。

### 3. 基础数据管理
*   **部门(位置)管理**：管理组织架构或物理位置信息（支持增删改查及批量删除）。
*   **管理员管理**：管理资产负责人或保管人信息（支持增删改查及批量删除）。

### 4. 系统日志
*   **操作日志**：自动记录关键操作（如新增、修改、删除、批量删除），包含操作时间、模块、详情及操作人，保障数据安全可追溯。

## 🛠️ 技术栈
*   **后端**：Java 17+, Spring Boot 3.x, Spring Data JPA (Hibernate)
*   **数据库**：MySQL 8.0+
*   **前端**：Thymeleaf 模板引擎, Bootstrap 5 UI 框架, Bootstrap Icons
*   **工具**：Maven, EasyExcel (用于 Excel 处理)

## 🚀 部署与运行步骤

### 1. 环境准备
确保本地环境已安装以下软件：
*   JDK 17 或更高版本
*   MySQL 8.0 或更高版本
*   Maven 3.6+

### 2. 数据库配置
1.  启动 MySQL 服务。
2.  默认情况下，系统会自动创建数据库 `asset_management`。
3.  打开项目配置文件 `src/main/resources/application.properties`，确认数据库连接信息：
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/asset_management?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&createDatabaseIfNotExist=true
    spring.datasource.username=root  # 修改为你的 MySQL 用户名
    spring.datasource.password=root  # 修改为你的 MySQL 密码
    ```

### 3. 启动项目
在项目根目录下打开终端，执行以下命令：

**方式一：使用 Maven 插件运行**
```bash
mvn spring-boot:run
```

**方式二：打包后运行**
```bash
mvn clean package
java -jar target/asset-management-0.0.1-SNAPSHOT.jar
```

### 4. 访问系统
项目启动成功后，打开浏览器访问：
*   地址：`http://localhost:8081` (默认端口，可在 application.properties 中修改)

## ⚠️ 注意事项
1.  **Excel 导入格式**：
    *   导入资产时，请确保 Excel 文件包含必要的列头（如：资产名称、资产类别、规格型号、资产编号、使用状态、资产放置位置等）。
    *   系统支持动态表头解析，但建议列名保持规范。
2.  **数据删除**：
    *   删除部门/位置时，如果该位置下仍有关联资产，系统会提示无法删除，请先转移资产。
    *   删除操作（特别是批量删除）不可恢复，请谨慎操作。
3.  **自动建表**：
    *   项目配置了 `spring.jpa.hibernate.ddl-auto=update`，启动时会自动更新数据库表结构，无需手动执行 SQL 脚本。

## 📂 项目结构
```
src/main/java/com/example/asset
├── controller      // 控制器层 (处理 HTTP 请求)
├── service         // 业务逻辑层
├── repository      // 数据访问层 (JPA)
├── entity          // 数据库实体类
├── dto             // 数据传输对象
├── listener        // Excel 导入监听器
└── util            // 工具类

src/main/resources
├── templates       // HTML 页面模板 (Thymeleaf)
└── application.properties // 项目配置文件
```
