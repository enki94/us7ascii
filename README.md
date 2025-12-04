# Oracle US7ASCII JDBC Driver Wrapper

이 라이브러리는 Oracle 데이터베이스가 `US7ASCII` 캐릭터셋으로 설정되어 있고, 애플리케이션이 `EUC-KR`(한글)을 사용할 때 발생하는 인코딩 깨짐 문제를 해결하기 위한 JDBC 드라이버 래퍼(Wrapper)입니다.

기존의 단순한 인코딩 변환(`new String(..., "8859_1")`)만으로는 해결되지 않는 Oracle JDBC 드라이버의 제약 사항을 우회하는 기법을 적용했습니다.

## 주요 기능 및 작동 원리

이 드라이버는 `jdbc:oracle:us7ascii:`로 시작하는 URL을 가로채서 동작하며, 다음과 같은 방식으로 한글을 처리합니다.

### 1. PreparedStatement 지원 (파라미터 바인딩)
Oracle JDBC 드라이버는 `setString()` 호출 시 클라이언트 캐릭터셋에 맞춰 변환을 시도하며, 이 과정에서 `US7ASCII` 범위를 벗어나는 문자가 손상될 수 있습니다.
이를 방지하기 위해 이 래퍼는 내부적으로 **`setAsciiStream()`**을 사용하여 변환 과정을 우회하고 **Raw Byte(EUC-KR)**를 데이터베이스로 직접 전송합니다.

### 2. Statement 지원 (SQL 리터럴)
`Statement`를 사용하여 SQL에 문자열 리터럴(`'한글'`)을 직접 포함하는 경우, 드라이버가 SQL 전체를 변환하면서 문자가 깨집니다.
이를 해결하기 위해 SQL을 파싱하여 문자열 리터럴을 감지하고, 이를 Oracle 내장 함수인 **`UTL_RAW.CAST_TO_VARCHAR2(HEXTORAW('...'))`** 형태로 자동 변환하여 실행합니다.
- 변환 전: `INSERT INTO table VALUES ('한글')`
- 변환 후: `INSERT INTO table VALUES (UTL_RAW.CAST_TO_VARCHAR2(HEXTORAW('C7D1B1DB')))`

### 3. COMMENT ON 지원
`COMMENT ON` 구문은 표준 SQL과 달리 함수 호출을 허용하지 않습니다. 이를 지원하기 위해 `COMMENT ON` 구문이 감지되면 자동으로 PL/SQL 블록(`BEGIN EXECUTE IMMEDIATE ... END;`)으로 감싸서 실행합니다.

### 4. 조회 (ResultSet)
데이터베이스에서 조회된 문자열(`ISO-8859-1`로 인식됨)을 다시 `EUC-KR`로 역변환하여 애플리케이션에 올바른 한글을 반환합니다.

## 요구 사항

- **Oracle Database**: `UTL_RAW` 패키지가 설치되어 있어야 합니다 (대부분의 Oracle DB에 기본 포함).
- **Oracle JDBC Driver**: `ojdbc` 라이브러리가 클래스패스에 존재해야 합니다.

## 사용 방법

### 1. 빌드 및 설치
```bash
mvn clean install
```
생성된 `us7ascii-jdbc-1.0.jar`를 프로젝트에 추가합니다.

### 2. 드라이버 설정

**JDBC URL 변경:**
반드시 `us7ascii:` 접두어를 추가해야 합니다.
- 기존: `jdbc:oracle:thin:@localhost:1521:XE`
- 변경: `jdbc:oracle:us7ascii:thin:@localhost:1521:XE`

**Driver Class:**
- `com.enki.jdbc.driver.Us7AsciiDriver`

### 3. 예제 코드

```java
Class.forName("com.enki.jdbc.driver.Us7AsciiDriver");
String url = "jdbc:oracle:us7ascii:thin:@localhost:1521:XE";
Connection conn = DriverManager.getConnection(url, "user", "password");

// 1. PreparedStatement 사용 (권장)
PreparedStatement pstmt = conn.prepareStatement("INSERT INTO test_table (col1) VALUES (?)");
pstmt.setString(1, "한글테스트"); // 자동으로 setAsciiStream으로 변환되어 전송됨
pstmt.executeUpdate();

// 2. Statement 사용
Statement stmt = conn.createStatement();
// 자동으로 UTL_RAW 변환되어 실행됨
stmt.executeUpdate("INSERT INTO test_table (col1) VALUES ('한글테스트')");

// 3. 조회
ResultSet rs = stmt.executeQuery("SELECT col1 FROM test_table");
while (rs.next()) {
    String val = rs.getString("col1"); // 자동으로 EUC-KR로 변환되어 반환됨
    System.out.println(val);
}
```

## 주의 사항
- 이 드라이버는 `US7ASCII` DB에 `EUC-KR` 데이터를 저장하는 특수한 상황("US7ASCII Hack")을 위해 제작되었습니다.
- `NVARCHAR`, `NCHAR` 등 유니코드 컬럼에 대해서는 테스트되지 않았습니다.

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://buymeacoffee.com/enki94)
