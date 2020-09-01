# shared-id-endpoint
Http service for the sharedid.org domain

### Run locally

```
./start.sh
```

### Build docker image
```
mvn clean compile package docker:build
```

### Push docker image
Requires proper authentication with AWS. Login using the AWS CLI, or make sure that AWS credentials can be found on the
host, either through environment variables or EC2 instance metadata.
```
mvn docker:push
```