
#!groovy

node {
  git url: 'https://github.com/dwamara/ConsulSDREE.git'
  def mvnHome = tool 'M3'
  sh "${mvnHome}/bin/mvn clean install"
}
