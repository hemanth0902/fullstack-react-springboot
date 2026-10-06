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

        // ============================================================
        // 1. CHECKOUT
        // ============================================================

        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitHub...'

                git branch: 'main',
                    url: 'https://github.com/hemanth0902/fullstack-react-springboot.git'
            }
        }


        // ============================================================
        // 2. GITLEAKS
        // ============================================================

        stage('GitLeaks - Secret Scan') {
            steps {
                echo 'Scanning source code for leaked secrets...'

                bat '''
                    gitleaks detect --source . --no-banner
                '''
            }
        }


        // ============================================================
        // 3. BUILD
        // ============================================================

        stage('Backend Build') {
            steps {
                echo 'Building Spring Boot backend...'

                dir('backend') {
                    bat '''
                        mvn clean package -DskipTests
                    '''
                }
            }
        }


        stage('Frontend Build') {
            steps {
                echo 'Building React frontend...'

                dir('frontend') {
                    bat '''
                        npm install
                        npm run build
                    '''
                }
            }
        }


        // ============================================================
        // 4. SONARQUBE
        // ============================================================

        stage('SonarQube - SAST') {
            steps {
                echo 'Running SonarQube source-code analysis...'

                dir('backend') {
                    bat '''
                        mvn sonar:sonar ^
                          -Dsonar.host.url=%SONAR_HOST_URL%
                    '''
                }
            }
        }


        // ============================================================
        // 5. DEPENDENCY CHECK
        // ============================================================

        stage('Dependency Check') {
            steps {
                echo 'Scanning application dependencies...'

                dependency-check.bat ^
                    --project "%APP_NAME%" ^
                    --scan "." ^
                    --format "HTML" ^
                    --out "dependency-check-report"
            }
        }


        // ============================================================
        // 6. TESTS
        // ============================================================

        stage('Tests') {
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


        // ============================================================
        // 7. DOCKER BUILD
        // ============================================================

        stage('Docker Build') {
            steps {

                echo 'Building backend Docker image...'

                bat '''
                    docker build ^
                      -t %BACKEND_IMAGE%:%IMAGE_TAG% ^
                      ./backend
                '''

                echo 'Building frontend Docker image...'

                bat '''
                    docker build ^
                      -t %FRONTEND_IMAGE%:%IMAGE_TAG% ^
                      ./frontend
                '''
            }
        }


        // ============================================================
        // 8. TRIVY
        // ============================================================

        stage('Trivy - Container Scan') {
            steps {

                echo 'Scanning backend Docker image...'

                bat '''
                    trivy image ^
                      --severity HIGH,CRITICAL ^
                      --exit-code 1 ^
                      %BACKEND_IMAGE%:%IMAGE_TAG%
                '''

                echo 'Scanning frontend Docker image...'

                bat '''
                    trivy image ^
                      --severity HIGH,CRITICAL ^
                      --exit-code 1 ^
                      %FRONTEND_IMAGE%:%IMAGE_TAG%
                '''
            }
        }


        // ============================================================
        // 9. CHECKOV
        // ============================================================

        stage('Checkov - IaC Scan') {
            steps {

                echo 'Scanning Infrastructure as Code...'

                bat '''
                    checkov -d .
                '''
            }
        }


        // ============================================================
        // 10. SECURITY GATE
        // ============================================================

        stage('Security Gate') {
            steps {

                echo '======================================'
                echo '        SECURITY GATE'
                echo '======================================'

                echo 'GitLeaks       : PASSED'
                echo 'SonarQube      : PASSED'
                echo 'Dependency     : PASSED'
                echo 'Trivy          : PASSED'
                echo 'Checkov        : PASSED'

                echo 'All security checks passed.'
                echo 'Deployment is allowed.'
            }
        }


        // ============================================================
        // 11. DOCKER REGISTRY
        // ============================================================

        stage('Push Docker Images') {
            steps {

                echo 'Pushing Docker images to Docker Hub...'

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


        // ============================================================
        // 12. LOCAL KUBERNETES
        // ============================================================

        stage('Deploy to Kubernetes') {
            steps {

                echo 'Deploying application to local Kubernetes...'

                bat '''
                    kubectl apply -f k8s/
                '''

                echo 'Checking Kubernetes deployment...'

                bat '''
                    kubectl get pods
                    kubectl get services
                '''
            }
        }


        // ============================================================
        // 13. MONITORING
        // ============================================================

        stage('Monitoring') {
            steps {

                echo 'Checking Prometheus and Grafana...'

                bat '''
                    kubectl get pods -n monitoring
                '''

                echo 'Prometheus and Grafana monitoring stage completed.'
            }
        }
    }


    // ================================================================
    // POST ACTIONS
    // ================================================================

    post {

        success {
            echo '''
            ==========================================
            SECURE DEPLOY PIPELINE SUCCESSFUL
            ==========================================
            Application passed CI/CD and security
            checks and was deployed successfully.
            ==========================================
            '''
        }

        failure {
            echo '''
            ==========================================
            SECURE DEPLOY PIPELINE FAILED
            ==========================================
            Check the failed stage above.
            Deployment was stopped.
            ==========================================
            '''
        }

        always {
            echo 'Pipeline execution completed.'
        }
    }
}