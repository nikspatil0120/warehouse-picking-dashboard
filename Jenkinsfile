
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

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
                bat '''
                    if not exist "%WAR_FILE%" (
                        echo ERROR: WAR file was not created.
                        exit /b 1
                    )
                    echo WAR created successfully.
                '''
            }
        }

        stage('Start Application for Selenium') {
            steps {
                powershell '''
                    $ErrorActionPreference = 'Stop'
                    $target = Join-Path $env:WORKSPACE 'target'
                    $log = Join-Path $target 'selenium-app.log'
                    $err = Join-Path $target 'selenium-app-error.log'
                    $pidFile = Join-Path $target 'selenium-app.pid'

                    New-Item -ItemType Directory -Force -Path $target | Out-Null
                    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue

                    Write-Output 'Launching Spring Boot on port 8081...'

                    # Use Start-Process with redirected streams.
                    # Escape Windows paths from Groovy by avoiding them
                    # inside the command string.
                    $args = '/c start "" /B mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081'
                    $launcher = Start-Process `
                        -FilePath 'cmd.exe' `
                        -ArgumentList $args `
                        -WorkingDirectory $env:WORKSPACE `
                        -RedirectStandardOutput $log `
                        -RedirectStandardError $err `
                        -WindowStyle Hidden `
                        -PassThru

                    Write-Output "Launcher command returned. PID: $($launcher.Id)"

                    $ready = $false
                    $appPid = $null

                    for ($i = 0; $i -lt 90; $i++) {
                        Start-Sleep -Seconds 2

                        try {
                            $response = Invoke-WebRequest `
                                -Uri 'http://127.0.0.1:8081/items' `
                                -UseBasicParsing `
                                -TimeoutSec 3

                            if ($response.StatusCode -eq 200) {
                                $ready = $true
                                $connection = Get-NetTCPConnection `
                                    -LocalPort 8081 `
                                    -State Listen `
                                    -ErrorAction SilentlyContinue |
                                    Select-Object -First 1

                                if ($connection) {
                                    $appPid = $connection.OwningProcess
                                }
                                break
                            }
                        } catch {}
                    }

                    if (-not $ready) {
                        Write-Output 'ERROR: Application did not become ready.'
                        if (Test-Path $log) {
                            Get-Content $log -Tail 80
                        }
                        if (Test-Path $err) {
                            Get-Content $err -Tail 80
                        }
                        exit 1
                    }

                    if ($appPid) {
                        Set-Content -Path $pidFile -Value $appPid
                    }

                    Write-Output 'Application is responding on port 8081.'
                    Write-Output 'Startup stage complete.'
                '''
            }
        }

        stage('Continuous Testing') {
            steps {
                bat 'mvn test'
            }

            post {
                always {
                    powershell(
                        returnStatus: true,
                        script: '''
                            $pidFile = Join-Path $env:WORKSPACE 'target\\selenium-app.pid'

                            try {
                                if (Test-Path $pidFile) {
                                    $appPid = Get-Content $pidFile -ErrorAction SilentlyContinue

                                    if ($appPid -and $appPid -match '^\\d+$') {
                                        Write-Output "Attempting to stop application PID $appPid"
                                        & taskkill.exe /PID ([int]$appPid) /T /F 2>$null
                                    }

                                    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
                                } else {
                                    Write-Output 'No application PID file found.'
                                }
                            } catch {
                                Write-Output "Cleanup warning: $($_.Exception.Message)"
                            }

                            exit 0
                        '''
                    )
                }
            }
        }

        stage('Package Verification') {
            steps {
                bat '''
                    if not exist "%WAR_FILE%" (
                        echo ERROR: Packaged WAR not found.
                        exit /b 1
                    )
                    echo WAR file verified successfully.
                '''

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
                    def deployDir =
                        env.TOMCAT_WEBAPPS_DIR ?: 'C:\\Tomcat\\webapps'

                    if (!fileExists(env.WAR_FILE)) {
                        error("WAR file not found: ${env.WAR_FILE}")
                    }

                    if (!fileExists(deployDir)) {
                        error("Tomcat deployment directory not found: ${deployDir}")
                    }

                    withEnv(["DEPLOY_DIR=${deployDir}"]) {
                        bat '''
                            if not exist "%WORKSPACE%\\target\\picking-dashboard-0.0.1-SNAPSHOT.war" (
                                echo ERROR: Source WAR not found.
                                exit /b 1
                            )

                            if not exist "%DEPLOY_DIR%" (
                                echo ERROR: Tomcat webapps directory not found.
                                exit /b 1
                            )

                            copy /Y "%WORKSPACE%\\target\\picking-dashboard-0.0.1-SNAPSHOT.war" "%DEPLOY_DIR%\\picking-dashboard.war"

                            if errorlevel 1 (
                                echo ERROR: WAR deployment copy failed.
                                exit /b 1
                            )

                            echo WAR copied successfully.
                        '''
                    }

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
            echo 'Pipeline succeeded. Tests passed; deployment ran if DEPLOY_ENV was test.'
        }

        failure {
            echo 'Pipeline failed. Review the failed stage and test reports.'
        }
    }
}
