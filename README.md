# Running the accountService
`sbt "; project accountService; run"`

or in development mode:

`sbt "; project accountService; ~reStart"`

# Running the newsletterService
`sbt "; project newsletterService; run"`

or in development mode:

`sbt "; project newsletterService; ~reStart"`


# Running tests with coverage
`sbt "; project newsletterService; clean; coverage; test; coverageReport"`

# Running API tests
First you will need to run the newsletter service (see above), then run this command:

`sbt newsletterServiceTest/test` 
 
 or

`sbt "; project newsletterServiceTest; clean; test"`

with environment config: 

`sbt -DCONFIG=test.conf newsletterServiceTest/test` 