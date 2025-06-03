#!/bin/bash

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Função para exibir mensagens
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verifica se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    print_error "Maven não encontrado. Por favor, instale o Maven primeiro."
    exit 1
fi

# Compila o projeto
print_message "Compilando o projeto..."
mvn clean compile

if [ $? -ne 0 ]; then
    print_error "Falha na compilação"
    exit 1
fi

# Executa os testes
print_message "Executando testes..."
mvn test

if [ $? -ne 0 ]; then
    print_error "Falha nos testes"
    exit 1
fi

# Gera o pacote
print_message "Gerando pacote..."
mvn package -DskipTests

if [ $? -ne 0 ]; then
    print_error "Falha ao gerar o pacote"
    exit 1
fi

# Executa a aplicação
print_message "Iniciando a aplicação..."
mvn spring-boot:run 