import sys

with open('docker-compose.yml', 'r') as f:
    data = f.read()

# Replace db block
old_db = """  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: revshop-postgres
    environment:
      POSTGRES_USER: revshop
      POSTGRES_PASSWORD: revshop123
      POSTGRES_DB: revshop
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - revshop-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U revshop"]
      interval: 10s
      timeout: 5s
      retries: 5"""

new_db = """  # MySQL Database
  mysql:
    image: mysql:8.0
    container_name: revshop-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: revshop
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - revshop-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5"""

if old_db in data:
    data = data.replace(old_db, new_db)

# Replace dependencies
data = data.replace("""    depends_on:
      postgres:
        condition: service_healthy""", """    depends_on:
      mysql:
        condition: service_healthy""")

# Replace environments
data = data.replace("""      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/revshop
      - SPRING_DATASOURCE_USERNAME=revshop
      - SPRING_DATASOURCE_PASSWORD=revshop123""", """      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/revshop?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root""")

# Replace volumes at the end
data = data.replace("""volumes:
  postgres-data:""", """volumes:
  mysql-data:""")

with open('docker-compose.yml', 'w') as f:
    f.write(data)
