pipeline {
  agent any
  stages {
    stage('Get Voucher Promotion') {
      steps {
        sh '''export M2_HOME=/usr/local/Cellar/maven/3.6.3_1/libexec
export PATH=$PATH:$M2_HOME/bin
mvn clean test -Dsurefire.suiteXmlFiles=zanuar-xml-list/remote-service-promotion/getVoucherPromotion.xml'''
      }
    }

    stage('Get My Voucher') {
      steps {
        sh '''export M2_HOME=/usr/local/Cellar/maven/3.6.3_1/libexec
export PATH=$PATH:$M2_HOME/bin
mvn clean test -Dsurefire.suiteXmlFiles=zanuar-xml-list/remote-service-promotion/getMyVoucher.xml'''
      }
    }

    stage('Get Voucher Detail') {
      steps {
        sh '''export M2_HOME=/usr/local/Cellar/maven/3.6.3_1/libexec
export PATH=$PATH:$M2_HOME/bin
mvn clean test -Dsurefire.suiteXmlFiles=zanuar-xml-list/remote-service-promotion/getVoucherDetail.xml'''
      }
    }

    stage('Unredeem') {
      steps {
        sh '''export M2_HOME=/usr/local/Cellar/maven/3.6.3_1/libexec
export PATH=$PATH:$M2_HOME/bin
mvn clean test -Dsurefire.suiteXmlFiles=zanuar-xml-list/remote-service-promotion/unredeem.xml'''
      }
    }

  }
}