#!groovy

String GIT_VERSION

node {
  def buildEnv
  def imageTag
  def projectName = 'boston-convertdata'
  def highDimensionServiceName = 'greenland-high-dimension'
  def mountName = 'boston-convertdata-data'
  def branchConfigMap = [
    master: [
      k8sServiceName: "${projectName}-prod",
      k8sHighDimensionServiceName: "${highDimensionServiceName}-prod",
      envName: "prod",
      mountName: "${mountName}"
    ],
    develop: [
      k8sServiceName: "${projectName}-test",
      k8sHighDimensionServiceName: "${highDimensionServiceName}-test",
      envName: "test",
      mountName: "${mountName}"
    ]
  ]

  def branchConfig = branchConfigMap[env.BRANCH_NAME]
  if (branchConfig == null) {
    return
  }

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
        imageTag="${projectName}:${uniqueId}"
        docker.build(imageTag).push()
      }
    }
  }

  if (branchConfig.k8sServiceName != null) {
    withCredentials([string(credentialsId: 'registry-address2', variable: 'registryAddress2')]) {
      stage('deploy') {
        docker.image('lachlanevenson/k8s-kubectl').inside {
          withCredentials(bindings: [[$class: "FileBinding", credentialsId: 'kubeconfig', variable: 'KUBE_CONFIG']]) {
            def kubectl = "kubectl --kubeconfig=\$KUBE_CONFIG"
            sh """
              cat k8s.yml | \
              sed 's~ENV_NAME_HERE~${branchConfig.envName}~g' | \
              sed 's~SERVER_IMAGE_TAG_HERE~${registryAddress2}/${imageTag}~g' | \
              sed 's~SERVICE_NAME_HERE~${branchConfig.k8sServiceName}~g' | \
              sed 's~HIGH_DIMENSION_NAME_HERE~${branchConfig.k8sHighDimensionServiceName}~g' | \
              sed 's~MOUNT_NAME~${branchConfig.mountName}~g' | \\
              ${kubectl} apply -f -
            """
          }
        }
      }
    }
  }
}
