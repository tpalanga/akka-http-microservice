# Running akka-http-microservice
`sbt run`

or in development mode:

`sbt ~reStart"`

# Running the dataservice
`sbt "; project dataservice; run"`

or in development mode:

`sbt "; project dataservice; ~reStart"`


# Running tests with coverage
`sbt "; project dataservice; clean; coverage; test; coverageReport"`