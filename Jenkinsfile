
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
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Package Verification') {
            steps {
                bat 'if not exist "%WAR_FILE%" exit /b 1'

                archiveArtifacts(
                    artifacts: 'target/*.war',
                    fingerprint: true
                )

                junit(
                    testResults: 'target/surefire-reports/*.xml',
                    allowEmptyResults: false
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
                    def warPath = "${env.WORKSPACE}\\${env.WAR_FILE}"

                    echo "Workspace: ${env.WORKSPACE}"
                    echo "WAR source: ${warPath}"
                    echo "Deployment directory: ${deployDir}"

                    if (!fileExists(env.WAR_FILE)) {
                        error("WAR file not found in workspace: ${env.WAR_FILE}")
                    }

                    if (!fileExists(deployDir)) {
                        error("Tomcat deployment directory not found: ${deployDir}")
                    }

                    bat """
                        echo === WORKSPACE ===
                        echo %WORKSPACE%

                        echo === WAR FILE CHECK ===
                        dir "%WORKSPACE%\\target\\picking-dashboard-0.0.1-SNAPSHOT.war"

                        if not exist "%WORKSPACE%\\target\\picking-dashboard-0.0.1-SNAPSHOT.war" (
                            echo ERROR: WAR file does not exist at the expected path.
                            exit /b 2
                        )

                        echo === TOMCAT DIRECTORY CHECK ===
                        dir "${deployDir}"

                        if not exist "${deployDir}\\." (
                            echo ERROR: Tomcat deployment directory is unavailable.
                            exit /b 3
                        )

                        echo === COPYING WAR ===
                        copy /Y "%WORKSPACE%\\target\\picking-dashboard-0.0.1-SNAPSHOT.war" "${deployDir}\\picking-dashboard.war"

                        if errorlevel 1 (
                            echo ERROR: WAR copy failed.
                            exit /b 4
                        )

                        echo === VERIFYING DEPLOYED WAR ===
                        dir "${deployDir}\\picking-dashboard.war"

                        if not exist "${deployDir}\\picking-dashboard.war" (
                            echo ERROR: Deployed WAR was not found.
                            exit /b 5
                        )
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for DEPLOY_ENV=${params.DEPLOY_ENV}."
        }

        failure {
            echo 'Pipeline failed. Review the stage logs.'
        }

        always {
            archiveArtifacts(
                artifacts: 'target/*.war',
                allowEmptyArchive: true,
                fingerprint: true
            )
        }
    }
}
