node {

    stage 'Checkout'

    git url: 'https://github.com/qwazr/library.git'

    stage 'Build' 

    withMaven(maven: 'Maven') {
        sh "mvn -U clean deploy"
    }

}
