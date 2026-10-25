
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

                        Write-Output 'Manually started application is responding.'
                    } catch {
                        Write-Output 'ERROR: Start the application manually in CMD first.'
                        Write-Output 'Command: mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"'
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
                artifacts: 'target/*.war,target/selenium-screenshots/*.png',
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
