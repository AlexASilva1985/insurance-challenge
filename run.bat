@echo off
echo [INFO] Verificando Maven...

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven nao encontrado. Por favor, instale o Maven primeiro.
    exit /b 1
)

echo [INFO] Compilando o projeto...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Falha na compilacao
    exit /b 1
)

echo [INFO] Executando testes...
call mvn test
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Falha nos testes
    exit /b 1
)

echo [INFO] Gerando pacote...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Falha ao gerar o pacote
    exit /b 1
)

echo [INFO] Iniciando a aplicacao...
call mvn spring-boot:run 