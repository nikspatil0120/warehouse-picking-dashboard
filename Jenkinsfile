
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
            description: 'local = build image only; test = publish and deploy Docker container'
        )
    }

    environment {
        APP_NAME = 'picking-dashboard'
        WAR_FILE = 'target/picking-dashboard-0.0.1-SNAPSHOT.war'
        SELENIUM_APP_PORT = '8081'
        DOCKERHUB_REPO = 'ryusseikun/picking-dashboard'
        CONTAINER_NAME = 'picking-dashboard-container'
        HOST_PORT = '8083'
        CONTAINER_PORT = '8080'
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

        stage('Verify Application') {
            steps {
                powershell '''
                    $url = 'http://127.0.0.1:8081/items'

                    try {
                        $response = Invoke-WebRequest `
                            -Uri $url `
                            -UseBasicParsing `
                            -TimeoutSec 5

                        if ($response.StatusCode -ne 200) {
                            throw "Unexpected HTTP status: $($response.StatusCode)"
                        }

                        Write-Output 'Application verification passed.'
                    } catch {
                        Write-Output 'ERROR: Application check on port 8081 failed.'
                        Write-Output 'Start the application before running this pipeline.'
                        exit 1
                    }
                '''
            }
        }

        stage('Continuous Testing') {
            steps {
                bat 'mvn test'
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

        stage('Docker Build') {
            steps {
                bat '''
                    docker build -t "%DOCKERHUB_REPO%:build-%BUILD_NUMBER%" .
                    if errorlevel 1 exit /b 1

                    docker image inspect "%DOCKERHUB_REPO%:build-%BUILD_NUMBER%"
                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('Docker Hub Push') {
            when {
                expression { params.DEPLOY_ENV == 'test' }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_TOKEN'
                    )
                ]) {
                    bat '''
                        @echo off
                        powershell -NoProfile -Command "$env:DOCKER_TOKEN | docker login -u $env:DOCKER_USER --password-stdin"
                        if errorlevel 1 exit /b 1

                        docker push "%DOCKERHUB_REPO%:build-%BUILD_NUMBER%"
                        if errorlevel 1 (
                            docker logout
                            exit /b 1
                        )

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy Container') {
            when {
                expression { params.DEPLOY_ENV == 'test' }
            }
            steps {
                bat '''
                    docker pull "%DOCKERHUB_REPO%:build-%BUILD_NUMBER%"
                    if errorlevel 1 exit /b 1

                    docker stop "%CONTAINER_NAME%" 2>NUL
                    docker rm "%CONTAINER_NAME%" 2>NUL

                    docker run -d --name "%CONTAINER_NAME%" -p %HOST_PORT%:%CONTAINER_PORT% "%DOCKERHUB_REPO%:build-%BUILD_NUMBER%"
                    if errorlevel 1 exit /b 1

                    docker ps
                    docker logs --tail 100 "%CONTAINER_NAME%"
                '''
            }
        }

        stage('Verify Docker Deployment') {
            when {
                expression { params.DEPLOY_ENV == 'test' }
            }
            steps {
                powershell '''
                    $url = 'http://127.0.0.1:8083/'
                    $success = $false

                    for ($i = 1; $i -le 30; $i++) {
                        try {
                            $response = Invoke-WebRequest `
                                -Uri $url `
                                -UseBasicParsing `
                                -TimeoutSec 5

                            if ($response.StatusCode -eq 200) {
                                $success = $true
                                break
                            }
                        } catch {
                            Start-Sleep -Seconds 2
                        }
                    }

                    if (-not $success) {
                        Write-Output 'ERROR: Docker application did not return HTTP 200.'
                        docker logs --tail 100 picking-dashboard-container
                        exit 1
                    }

                    Write-Output 'Docker deployment verified: HTTP 200.'
                '''
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
                artifacts: 'target/*.war,target/selenium-screenshots/*.png',
                allowEmptyArchive: true,
                fingerprint: true
            )
        }

        success {
            echo "Pipeline succeeded for DEPLOY_ENV=${params.DEPLOY_ENV}."
        }

        failure {
            echo 'Pipeline failed. Review the failed stage and test reports.'
        }
    }
}
