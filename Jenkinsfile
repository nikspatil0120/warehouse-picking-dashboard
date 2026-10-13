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
        powershell '''
            $ErrorActionPreference = 'Stop'
            $log = Join-Path $env:WORKSPACE 'target\\selenium-app.log'
            $err = Join-Path $env:WORKSPACE 'target\\selenium-app-error.log'
            $pidFile = Join-Path $env:WORKSPACE 'target\\selenium-app.pid'

            New-Item -ItemType Directory -Force -Path (Join-Path $env:WORKSPACE 'target') | Out-Null

            if (Test-Path $pidFile) {
                $oldPid = Get-Content $pidFile -ErrorAction SilentlyContinue
                if ($oldPid) {
                    taskkill /PID $oldPid /T /F 2>$null | Out-Null
                }
                Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
            }

            $proc = Start-Process -FilePath 'cmd.exe' `
                -ArgumentList '/c', 'mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"' `
                -WorkingDirectory $env:WORKSPACE `
                -RedirectStandardOutput $log `
                -RedirectStandardError $err `
                -PassThru

            Set-Content -Path $pidFile -Value $proc.Id

            $ready = $false
            for ($i = 0; $i -lt 90; $i++) {
                Start-Sleep -Seconds 2
                $proc.Refresh()

                if ($proc.HasExited) {
                    Write-Output 'ERROR: Application launcher exited early.'
                    if (Test-Path $log) { Get-Content $log -Tail 80 }
                    if (Test-Path $err) { Get-Content $err -Tail 80 }
                    exit 1
                }

                try {
                    $response = Invoke-WebRequest `
                        -Uri 'http://127.0.0.1:8081/items' `
                        -UseBasicParsing -TimeoutSec 3

                    if ($response.StatusCode -eq 200) {
                        $ready = $true
                        break
                    }
                } catch {}
            }

            if (-not $ready) {
                Write-Output 'ERROR: Application did not become ready on port 8081.'
                if (Test-Path $log) { Get-Content $log -Tail 100 }
                if (Test-Path $err) { Get-Content $err -Tail 100 }
                exit 1
            }

            Write-Output 'Application is responding on port 8081.'
        '''
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