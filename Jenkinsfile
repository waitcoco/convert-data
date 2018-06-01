#!groovy

String GIT_VERSION

node {
  def buildEnv

  stage ('Checkout') {
    checkout scm
    GIT_VERSION = sh (
      script: 'git rev-parse HEAD',
      returnStdout: true
    ).trim()
  }

  def now = new Date()
  def nowString = now.format("yyyyMMddHHmmss", TimeZone.getTimeZone('UTC'))
  def uniqueId = "${GIT_VERSION}_${nowString}"

  stage ('Build Custom Environment') {
    buildEnv = docker.build("build_env:${uniqueId}", 'build-env')
  }

  buildEnv.inside {
    stage('build') {
      env.GRADLE_USER_HOME = env.WORKSPACE
      sh 'gradle clean'
      sh 'gradle build'
    }
  }

  withCredentials([string(credentialsId: 'registry-address', variable: 'registryAddress')]) {
    stage('docker build') {
      docker.withRegistry("${registryAddress}") {
        if (env.BRANCH_NAME == 'master') {
          docker.build("boston-convertdata:${uniqueId}").push()
        }
      }
    }
  }

  if (env.BRANCH_NAME == 'master') {
    withCredentials([string(credentialsId: 'registry-address2', variable: 'registryAddress2')]) {
      stage('deploy') {
        docker.image('lachlanevenson/k8s-kubectl').inside {
          withCredentials(bindings: [[$class: "FileBinding", credentialsId: 'kubeconfig', variable: 'KUBE_CONFIG']]) {
            def kubectl = "kubectl --kubeconfig=\$KUBE_CONFIG"
            sh "sed 's~SERVER_IMAGE_TAG_HERE~${registryAddress2}/boston-convertdata:${uniqueId}~g;' k8s.yml | ${kubectl} apply -f -"
          }
        }
      }
    }
  }
}
