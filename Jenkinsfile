pipeline {
  agent any
  stages {
    stage('Get All Catalog') {
      steps {
        sh '''export M2_HOME=/usr/local/Cellar/maven/3.6.3_1/libexec
export PATH=$PATH:$M2_HOME/bin
mvn clean test -Dsurefire.suiteXmlFiles=zanuar-xml-list/remote-service-order/getAllCatalog.xml'''
      }
    }

  }
}