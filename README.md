# Testing microservices built with Akka HTTP and Akka actors

This is a project supporting my presentation on various methods of testing Akka-HTTP microservices.

Slides: 
 - slides-akka-microservice-testing.html
 - slides-docker-integration-testing.html

Video: https://skillsmatter.com/skillscasts/9640-london-scala-february-meetup

Feel free to use this code in your projects or to contact me with any kind of feedback.

## Running unit tests with coverage
```
sbt "; project accountService; clean; coverage; test; coverageReport"
sbt "; project newsletterService; clean; coverage; test; coverageReport"
```

## Running the services in Docker
```
sbt docker:publishLocal
docker-compose -f docker-compose.yml up -d
```

## Running with sbt
### The account service

`sbt "; project accountService; run"`

or in development mode:

`sbt "; project accountService; ~reStart"`

### The newsletter service
`sbt "; project newsletterService; run"`

or in development mode:

`sbt "; project newsletterService; ~reStart"`


## Running API tests
First you will need to run the newsletter service (see above), then run this command:

`sbt newsletterServiceTest/test` 
 
 or

`sbt "; project newsletterServiceTest; clean; test"`

with the CI environment config (against the Docker containers): 

`sbt -DDOCKER_IP=192.168.99.100 -DCONFIG=ci.conf newsletterServiceTest/test` 

The DOCKER_IP environment variable can be configured in your shell profile (bashrc or zshrc):

`export DOCKER_IP=192.168.99.100`