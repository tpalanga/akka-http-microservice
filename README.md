# Running akka-http-microservice
`sbt run`

or in development mode:

`sbt ~reStart"`

# Running the newsletterService
`sbt "; project newsletterService; run"`

or in development mode:

`sbt "; project newsletterService; ~reStart"`


# Running tests with coverage
`sbt "; project newsletterService; clean; coverage; test; coverageReport"`

# Running API tests
First you will need to run the newsletter service (see above), then run this command:

`sbt newsletterServiceTest/test`
