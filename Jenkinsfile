pipeline {
    agent any
    
    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build All Versions') {
            steps {
                script {
                    sh 'chmod +x gradlew'
                    def buildNumberProp = "${env.BUILD_NUMBER}"
                    sh "./gradlew buildAllVersions -PbuildNumber=${buildNumberProp}"
                }
            }
        }

        stage('List Built Files') {
            steps {
                script {
                    echo "Listing all files in build/libs recursively:"
                    sh "find build/libs -type f"
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'build/libs/**/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
