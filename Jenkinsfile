
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

                    if (!deployDir?.trim()) {
                        error(
                            'Configure TOMCAT_WEBAPPS_DIR in Jenkins before deployment.'
                        )
                    }

                    if (!fileExists(env.WAR_FILE)) {
                        error("WAR file not found: ${env.WAR_FILE}")
                    }

                    if (!fileExists(deployDir)) {
                        error("Tomcat deployment directory not found: ${deployDir}")
                    }

                    bat "copy /Y \"${env.WAR_FILE}\" \"${deployDir}\\picking-dashboard.war\""
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
