# archive

The microservice allows CRUD of `organisation` items with a name.

## End points

Method | Url            | Name
------ | -------------- | -----------
GET    | `/organisations`      | Returns all `organisation`s.
GET    | `/organisations/{id}` | Returns the `organisaton` for given id; or returns 404.
POST   | `/organisations`      | Creates an `organisation` from given JSON body; returns 201 with the created `organisation`.
PUT    | `/organisations/{id}` | Updates an existing `organisation` from given JSON body; returns 200 with the updated `organisation`; 404 otherwise.
DELETE | `/organisations/{id}` | Deletes the `organisation` with the specified id; returns 404 if not present.

Here are some examples on how to use the microservice with curl, assuming it runs on the default port 8080:

Create a Organisation:
```curl -X POST --header "Content-Type: application/json" --data '{"name": "my Organisation", "importance": "high"}' http://localhost:8080/organisations```

Get all Organisations:
```curl http://localhost:8080/organisations```

Get a single Organisation (assuming the id of the Organisation is 1):
```curl http://localhost:8080/organisations/1```

Update a Organisation (assuming the id of the Organisation is 1):
```curl -X PUT --header "Content-Type: application/json" --data '{"name": "my Organisation", "importance": "low"}' http://localhost:8080/organisations/1```

Delete a Organisation (assuming the id of the Organisation is 1):
```curl -X DELETE http://localhost:8080/organisations/1```

## Database
[h2](http://www.h2database.com/) is used as a database. This is an in memory database, so when stopping the application, the state of the microservice is lost.

## Running
You can run the microservice with `sbt run`. By default it listens to port number 8080, you can change this in the `application.conf`.

