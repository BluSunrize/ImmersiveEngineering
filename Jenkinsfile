#!/usr/bin/env groovy

pipeline {
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '10'))
    }
    agent any
    tools {
        jdk "jdk-17"
    }
    stages {
        stage('Clean') {
            steps {
                echo 'Cleaning Project'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }
        stage('Build and Deploy') {
            environment {
                ie_add_git_rev = '1'
            }
            steps {
                echo 'Building and Deploying to Maven'
                script {
                    sh './gradlew publish'
                }
            }
        }
    }
    post {
        always {
            archive 'build/libs/**.jar'
        }
    }
}
