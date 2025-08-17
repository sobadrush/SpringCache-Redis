# Spring Boot 快取整合 Redis 專案

## 1. 專案目的

本專案旨在展示如何透過 Spring Boot 框架整合 Spring Cache，並使用 Redis 作為高效能的快取後端。專案的核心目標是演示一個健壯的快取解決方案，透過減少對底層資料來源的直接存取來提升應用程式效能。

主要展示了以下技術重點：

- **快取抽象**：使用 Spring Cache 的 `@Cacheable` 註解，以宣告式的方式為服務層方法加入快取邏輯。
- **Redis 整合**：設定 Spring Data Redis 與 Lettuce 客戶端，將快取資料儲存於 Redis 中。
- **高可用性**：配置 Redis Sentinel，確保快取服務的穩定性與高可用性。
- **多資料庫應用**：設定多個 `CacheManager` Bean，將不同用途的快取分別存放在不同的 Redis 資料庫中，實現業務隔離。
- **複雜物件序列化**：解決了在快取過程中，對包含**集合 (Collection)**、**巢狀物件 (Nested Objects)** 以及**循環引用 (Circular Dependencies)** 的複雜 Java 物件進行 JSON 序列化的常見問題。

## 2. 程式架構

本專案基於一個標準的 Spring Boot 專案結構，其核心技術棧包括：

- **核心框架**：Spring Boot
- **快取**：Spring Cache Abstraction
- **Redis 整合**：Spring Data Redis
- **Redis 客戶端**：Lettuce
- **Redis 高可用方案**：Redis Sentinel
- **JSON 序列化**：Jackson
- **建構工具**：Maven

## 3. 檔案功能說明

以下是專案中主要檔案與目錄的功能簡介：

- `pom.xml`
  - Maven 專案設定檔，定義了所有專案依賴，如 Spring Boot、Spring Data Redis、Jackson 等。

- `launch_env_variables.env`
  - 環境變數設定檔。用於設定 Redis 連線資訊，如 Sentinel 主機、密碼等。

- `src/main/resources/application.yml`
  - Spring Boot 應用程式的主要設定檔。

- `src/main/java/com/roger/springcacheredis/`
  - **`SpringCacheRedisApplication.java`**
    - Spring Boot 應用程式的進入點。
  - **`RedisConfig.java`**
    - **Redis 連線設定**：集中管理 LettuceConnectionFactory 的建立。
    - 支援 Sentinel 模式，並能從環境變數讀取設定。
    - 建立了多個 `LettuceConnectionFactory` Bean，分別對應到不同的 Redis 資料庫 (DB 0, 6, 8)。
  - **`CacheConfig.java`**
    - **快取核心設定**：集中管理 `CacheManager` 的建立。
    - 透過注入不同的 `LettuceConnectionFactory`，建立了多個 `CacheManager` Bean (`primaryCacheManager`, `empCacheManager`, `deptCacheManager`)，實現快取分庫管理。
    - **客製化 `ObjectMapper`**：為了解決複雜物件的序列化問題，此處對 Jackson 的 `ObjectMapper` 進行了客製化設定，是整個專案的關鍵：
      - `SerializationFeature.WRAP_ROOT_VALUE`：啟用根元素包裝，讓 List 等集合類型能被一個 JSON Object 包裹，從而可以附加 `@class` 類型資訊。
      - `DeserializationFeature.UNWRAP_ROOT_VALUE`：在反序列化時，自動解開被包裝的根元素。
      - `activateDefaultTyping`：啟用預設類型處理，並指定使用 `@class` 屬性 (`JsonTypeInfo.As.PROPERTY`) 來儲存類型資訊，確保反序列化時能還原成正確的 Java 類別。
  - **`entities/`**
    - **`EmpVO.java`** 和 **`DeptVO.java`**
      - 資料實體類別。
      - 使用 `@JsonManagedReference` 和 `@JsonBackReference` 註解來解決 `EmpVO` 與 `DeptVO` 之間的雙向關聯所導致的**循環引用**問題，確保 Jackson 能夠正確序列化。
  - **`service/`**
    - **`DeptService.java`**
      - 業務邏輯層。
      - `getEmpsByDeptId` 方法使用了 `@Cacheable` 註解，當此方法被呼叫時，Spring Cache 會自動處理快取的讀取與寫入邏輯。
      - 此處將回傳值改為 `collect(Collectors.toList())`，確保回傳的是一個標準的 `ArrayList`，以避免 `List.of()` 產生的不可變 List 可能與 Jackson 之間的交互問題。

- `src/test/java/com/roger/springcacheredis/`
  - **`DeptServiceTests.java`**
    - `DeptService` 的整合測試類別，用於驗證快取是否如預期般運作。

## 4. 執行步驟

### 4.1. 環境準備

本專案的測試與執行，需要一個包含 Master/Slave 複寫及 Sentinel 哨兵機制的 Redis 環境。

- **A. 啟動 Redis 環境 (推薦)**
  - 您可以參考以下 GitHub 專案，使用 Docker Compose 快速啟動一個完整的 Redis Sentinel 環境：
    - **專案地址**：[https://github.com/sobadrush/redis_mode_test.git](https://github.com/sobadrush/redis_mode_test.git)
  - **啟動步驟**：
    1.  Clone 該專案：`git clone https://github.com/sobadrush/redis_mode_test.git`
    2.  進入專案目錄：`cd redis_mode_test`
    3.  首先，啟動 Master/Slave 節點： `cd ..../redis_mode_test/redis_test`
        - Windows: `docker-compose -f docker-compose-redis-MasterSlave-password-named_volume.yaml up -d`
        - Linux/MacOS: `docker-compose -f docker-compose-redis-MasterSlave-password-bind_mount.yaml up -d`
    4.  然後，回到上一層並啟動 Sentinel 節點： `cd ..../redis_mode_test/redis_sentinel `
        `docker-compose -f docker-compose-sentinel.yaml up -d`
    5.  完成後，您將擁有一個在本機執行的、包含 1 個 Master、2 個 Slave、3 個 Sentinel 的完整 Redis 叢集。

- **B. 其他環境**
  - 確認已安裝 Java (建議 17 或以上版本) 及 Maven。
  - 如果您使用自己的 Redis 環境，請確保網路連線可通，並將連線資訊配置在 `launch_env_variables.env` 中。

### 4.2. 設定環境變數

根據你的 Redis 環境，修改 `launch_env_variables.env` 檔案中的設定值。若使用上述 Docker Compose 環境，則可能無需修改。

在終端機中執行以下命令，載入環境變數：
```bash
source launch_env_variables.env
```

### 4.3. 建構專案

在專案根目錄執行 Maven 命令進行建構：
```bash
mvn clean install
```

### 4.4. 執行應用程式

你可以透過以下任一方式執行：
```bash
# 方式一：使用 Spring Boot Maven 外掛
mvn spring-boot:run

# 方式二：執行打包好的 JAR 檔
java -jar target/spring-cache-redis-*.jar
```

## 5. 測試

本專案的測試是整合測試，需要依賴一個正在執行的 Redis 實例。

1.  **執行所有測試**
    - 在專案根目錄執行 Maven 命令：
      ```bash
      mvn test
      ```

2.  **核心測試說明 (`DeptServiceTests`)**
    - 這個測試會多次呼叫 `deptService.getEmpsByDeptId()` 方法。
    - **驗證方式**：
      - **首次呼叫**：由於快取中沒有資料，方法本身的邏輯會被執行，日誌中會印出從 `MockData` 取得的員工資訊。
      - **後續呼叫**：方法會直接從 Redis 快取中取得資料並回傳，方法本身的邏輯不會被執行。你可以透過觀察日誌輸出，或是在方法內設定中斷點來驗證這一點。
    - **重要提示**：在每次測試前，建議清除 Redis 中相關的快取資料，以避免舊的、格式不正確的快取影響測試結果。你可以使用 `redis-cli` 的 `FLUSHDB` 命令來清空當前資料庫。
