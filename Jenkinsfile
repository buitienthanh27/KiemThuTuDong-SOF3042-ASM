pipeline {
  agent any

  tools {
    jdk   'jdk17'
    maven 'maven'
  }

  environment {
    APP_PORT = '9090'
    BASE_URL = "http://localhost:9090"
  }

  options {
    timeout(time: 40, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build App') {
      steps {
        bat 'mvn clean package -DskipTests'
      }
    }

    stage('Start App (Simple)') {
      steps {
        // Cách nông dân: Chạy ngầm và chờ cứng 30s cho app lên
        powershell '''
          echo "Starting Spring Boot..."
          $args = "spring-boot:run", "-Dspring-boot.run.arguments=--server.port=$env:APP_PORT"
          
          # Start process và ghi log ra file (không tách log lỗi nữa cho đỡ rắc rối)
          Start-Process mvn -ArgumentList $args -RedirectStandardOutput "app.log" -WindowStyle Hidden
          
          echo "Waiting 30 seconds for App to startup..."
          Start-Sleep -Seconds 30
        '''
      }
    }

    stage('Run UI Tests') {
      steps {
        bat 'mvn test -DbaseUrl=%BASE_URL%'
      }
    }

    stage('Archive Reports') {
      steps {
        archiveArtifacts artifacts: 'test-output/**/*', allowEmptyArchive: true
        archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
        archiveArtifacts artifacts: 'app.log', allowEmptyArchive: true
      }
    }
  }

  post {
    always {
      echo 'Stopping App...'
      // Kill toàn bộ tiến trình Java (Reset máy sạch sẽ)
      powershell 'Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue'
    }
  }
}