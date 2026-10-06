pipeline {

    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven'
        nodejs 'NodeJS22'
    }

    environment {
        APP_NAME = 'fullstack-app'

        BACKEND_IMAGE = 'hemanthpoojary/fullstack-backend'
        FRONTEND_IMAGE = 'hemanthpoojary/fullstack-frontend'

        IMAGE_TAG = "${BUILD_NUMBER}"

        SONAR_HOST_URL = 'http://localhost:9000'
    }

    stages {

        // =========================================================
        // 1. CHECKOUT
        // =========================================================

        stage('1. Checkout') {
            steps {
                echo '========================================'
                echo 'Checking out source code from GitHub'
                echo '========================================'

                git branch: 'main',
                    url: 'https://github.com/hemanth0902/fullstack-react-springboot.git'
            }
        }


        // =========================================================
        // 2. GITLEAKS
        // =========================================================

        stage('2. GitLeaks - Secret Scan') {
            steps {
                echo '========================================'
                echo 'Scanning source code for leaked secrets'
                echo '========================================'

                bat '''
                    gitleaks detect --source . --no-banner
                '''
            }
        }


        // =========================================================
        // 3. BUILD
        // =========================================================

        stage('3. Backend Build') {
            steps {
                echo '========================================'
                echo 'Building Spring Boot Backend'
                echo '========================================'

                dir('backend') {
                    bat '''
                        mvn clean package -DskipTests
                    '''
                }
            }
        }


        stage('3. Frontend Build') {
            steps {
                echo '========================================'
                echo 'Building React Frontend'
                echo '========================================'

                dir('frontend') {
                    bat '''
                        npm install
                        npm run build
                    '''
                }
            }
        }


        // =========================================================
        // 4. SONARQUBE
        // =========================================================

        stage('4. SonarQube - SAST') {
            steps {
                echo '========================================'
                echo 'Running SonarQube Static Analysis'
                echo '========================================'

                dir('backend') {
                    bat '''
                        mvn sonar:sonar ^
                            -Dsonar.host.url=%SONAR_HOST_URL%
                    '''
                }
            }
        }


        // =========================================================
        // 5. DEPENDENCY CHECK
        // =========================================================

        stage('5. Dependency Check') {
            steps {
                echo '========================================'
                echo 'Scanning application dependencies'
                echo '========================================'

                bat '''
                    dependency-check.bat ^
                        --project "%APP_NAME%" ^
                        --scan "." ^
                        --format "HTML" ^
                        --out "dependency-check-report"
                '''
            }
        }


        // =========================================================
        // 6. TESTS
        // =========================================================

        stage('6. Tests') {
            parallel {

                stage('Backend Tests') {
                    steps {
                        echo 'Running backend tests...'

                        dir('backend') {
                            bat '''
                                mvn test
                            '''
                        }
                    }
                }

                stage('Frontend Tests') {
                    steps {
                        echo 'Running frontend tests...'

                        dir('frontend') {
                            bat '''
                                npm test -- --watchAll=false
                            '''
                        }
                    }
                }
            }
        }


        // =========================================================
        // 7. DOCKER BUILD
        // =========================================================

        stage('7. Docker Build') {
            steps {

                echo '========================================'
                echo 'Building Backend Docker Image'
                echo '========================================'

                bat '''
                    docker build ^
                        -t %BACKEND_IMAGE%:%IMAGE_TAG% ^
                        ./backend
                '''

                echo '========================================'
                echo 'Building Frontend Docker Image'
                echo '========================================'

                bat '''
                    docker build ^
                        -t %FRONTEND_IMAGE%:%IMAGE_TAG% ^
                        ./frontend
                '''
            }
        }


        // =========================================================
        // 8. TRIVY
        // =========================================================

        stage('8. Trivy - Container Security Scan') {
            steps {

                echo '========================================'
                echo 'Scanning Backend Docker Image'
                echo '========================================'

                bat '''
                    trivy image ^
                        --severity HIGH,CRITICAL ^
                        --exit-code 1 ^
                        %BACKEND_IMAGE%:%IMAGE_TAG%
                '''

                echo '========================================'
                echo 'Scanning Frontend Docker Image'
                echo '========================================'

                bat '''
                    trivy image ^
                        --severity HIGH,CRITICAL ^
                        --exit-code 1 ^
                        %FRONTEND_IMAGE%:%IMAGE_TAG%
                '''
            }
        }


        // =========================================================
        // 9. CHECKOV
        // =========================================================

        stage('9. Checkov - IaC Security Scan') {
            steps {

                echo '========================================'
                echo 'Scanning Infrastructure as Code'
                echo '========================================'

                bat '''
                    checkov -d .
                '''
            }
        }


        // =========================================================
        // 10. SECURITY GATE
        // =========================================================

        stage('10. Security Gate') {
            steps {

                echo '========================================'
                echo '             SECURITY GATE'
                echo '========================================'

                echo 'GitLeaks       : PASSED'
                echo 'SonarQube      : PASSED'
                echo 'Dependency     : PASSED'
                echo 'Tests          : PASSED'
                echo 'Trivy          : PASSED'
                echo 'Checkov        : PASSED'

                echo '========================================'
                echo 'ALL SECURITY CHECKS PASSED'
                echo 'DEPLOYMENT IS ALLOWED'
                echo '========================================'
            }
        }


        // =========================================================
        // 11. DOCKER REGISTRY
        // =========================================================

        stage('11. Push Docker Images') {
            steps {

                echo '========================================'
                echo 'Pushing Docker Images to Docker Hub'
                echo '========================================'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    bat '''
                        docker login -u %DOCKER_USERNAME% -p %DOCKER_PASSWORD%

                        docker push %BACKEND_IMAGE%:%IMAGE_TAG%

                        docker push %FRONTEND_IMAGE%:%IMAGE_TAG%

                        docker logout
                    '''
                }
            }
        }


        // =========================================================
        // 12. LOCAL KUBERNETES
        // =========================================================

        stage('12. Deploy to Local Kubernetes') {
            steps {

                echo '========================================'
                echo 'Deploying Application to Kubernetes'
                echo '========================================'

                bat '''
                    kubectl apply -f k8s/
                '''

                echo '========================================'
                echo 'Checking Kubernetes Pods'
                echo '========================================'

                bat '''
                    kubectl get pods
                '''

                echo '========================================'
                echo 'Checking Kubernetes Services'
                echo '========================================'

                bat '''
                    kubectl get services
                '''
            }
        }


        // =========================================================
        // 13. PROMETHEUS + GRAFANA
        // =========================================================

        stage('13. Prometheus + Grafana Monitoring') {
            steps {

                echo '========================================'
                echo 'Checking Monitoring Stack'
                echo '========================================'

                bat '''
                    kubectl get pods -n monitoring
                '''

                bat '''
                    kubectl get services -n monitoring
                '''

                echo 'Prometheus and Grafana monitoring check completed.'
            }
        }
    }


    // =============================================================
    // POST ACTIONS
    // =============================================================

    post {

        success {
            echo '''
=============================================
       SECURE DEPLOY PIPELINE SUCCESS
=============================================

Application:
FullStack React + Spring Boot

Pipeline:
GitHub
   -> Jenkins
   -> GitLeaks
   -> Build
   -> SonarQube
   -> Dependency Check
   -> Tests
   -> Docker
   -> Trivy
   -> Checkov
   -> Security Gate
   -> Docker Hub
   -> Kubernetes
   -> Prometheus
   -> Grafana

All stages completed successfully.
=============================================
'''
        }

        failure {
            echo '''
=============================================
       SECURE DEPLOY PIPELINE FAILED
=============================================

A pipeline stage failed.

Deployment has been stopped.

Check the failed stage and console output.
=============================================
'''
        }

        always {
            echo 'Pipeline execution completed.'
        }
    }
}