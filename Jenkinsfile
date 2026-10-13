pipeline {
    agent any

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['local', 'test'],
            description: 'Choose local build or Tomcat test deployment'
        )
    }

    environment {
        APP_NAME = 'picking-dashboard'
        WAR_FILE = 'target/picking-dashboard-0.0.1-SNAPSHOT.war'
        SELENIUM_APP_PORT = '8081'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat 'git branch --show-current'
            }
        }

        stage('Build Package') {
            steps {
                bat 'mvn clean package -DskipTests'
                bat 'if not exist "%WAR_FILE%" exit /b 1'
            }
        }

        stage('Start Application for Selenium') {
            steps {
                bat '''
                    if not exist target mkdir target
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$p = Start-Process -FilePath 'cmd.exe' -ArgumentList '/c', 'mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081' -WorkingDirectory $env:WORKSPACE -RedirectStandardOutput (Join-Path $env:WORKSPACE 'target\\selenium-app.log') -RedirectStandardError (Join-Path $env:WORKSPACE 'target\\selenium-app-error.log') -PassThru; Set-Content -Path (Join-Path $env:WORKSPACE 'target\\selenium-app.pid') -Value $p.Id"
                '''

                bat '''
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ready = $false; for ($i = 0; $i -lt 60; $i++) { try { $r = Invoke-WebRequest -Uri 'http://localhost:8081/items' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { $ready = $true; break } } catch {}; Start-Sleep -Seconds 2 }; if (-not $ready) { Write-Error 'Application did not become ready on port 8081'; Get-Content (Join-Path $env:WORKSPACE 'target\\selenium-app.log') -ErrorAction SilentlyContinue; exit 1 }"
                '''
            }

            post {
                always {
                    bat '''
                        powershell -NoProfile -ExecutionPolicy Bypass -Command "$f = Join-Path $env:WORKSPACE 'target\\selenium-app.pid'; if (Test-Path $f) { $id = Get-Content $f; if ($id) { taskkill /PID $id /T /F 2>$null | Out-Null }; Remove-Item $f -Force -ErrorAction SilentlyContinue }"
                    '''
                }
            }
        }

        stage('Continuous Testing') {
    steps {
        bat 'mvn test'
    }

    post {
        always {
            bat '''
                powershell -NoProfile -ExecutionPolicy Bypass -Command "$f = Join-Path $env:WORKSPACE 'target\\selenium-app.pid'; if (Test-Path $f) { $id = Get-Content $f; if ($id) { taskkill /PID $id /T /F 2>$null | Out-Null }; Remove-Item $f -Force -ErrorAction SilentlyContinue }"
            '''
        }
    }
}

        stage('Package Verification') {
            steps {
                bat 'if not exist "%WAR_FILE%" exit /b 1'
                archiveArtifacts(
                    artifacts: 'target/*.war',
                    fingerprint: true
                )
            }
        }

        stage('Deploy') {
            when {
                expression {
                    return params.DEPLOY_ENV == 'test'
                }
            }

            steps {
                script {
                    def deployDir = env.TOMCAT_WEBAPPS_DIR ?: 'C:\\Tomcat\\webapps'

                    if (!fileExists(env.WAR_FILE)) {
                        error("WAR file not found: ${env.WAR_FILE}")
                    }

                    if (!fileExists(deployDir)) {
                        error("Tomcat deployment directory not found: ${deployDir}")
                    }

                    bat '''
    if not exist "%WORKSPACE%\\target\\picking-dashboard-0.0.1-SNAPSHOT.war" (
        echo ERROR: Source WAR not found
        exit /b 1
    )

    if not exist "C:\\Tomcat\\webapps" (
        echo ERROR: Tomcat webapps folder not found
        exit /b 1
    )

    copy /Y "%WORKSPACE%\\target\\picking-dashboard-0.0.1-SNAPSHOT.war" "C:\\Tomcat\\webapps\\picking-dashboard.war"

    if errorlevel 1 exit /b 1
'''

                    echo "WAR deployed to ${deployDir}\\picking-dashboard.war"
                }
            }
        }
    }

    post {
        always {
            junit(
                testResults: 'target/surefire-reports/*.xml',
                allowEmptyResults: false
            )

            archiveArtifacts(
                artifacts: 'target/*.war,target/selenium-app*.log,target/selenium-screenshots/*.png',
                allowEmptyArchive: true,
                fingerprint: true
            )
        }

        success {
            echo 'Pipeline succeeded. Tests passed; deployment was allowed if DEPLOY_ENV was test.'
        }

        failure {
            echo 'Pipeline failed. Later stages, including deployment, will not run after a failed test stage.'
        }
    }
}