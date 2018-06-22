#!/bin/bash

# This script is used to deploy Knub to knub.herokuapp.com
# Step 1 : Build the app
gradle clean build
# Step 2 : Build to docker
sudo docker build -t knub .
# Step 3 : Push to heroku & docker.hub
# Push the built image on docker.hub
docker tag `docker inspect --format="{{.Id}}" knub` ganzf/kotlin-sample
docker push ganzf/kotlin-sample
sudo heroku container:push web
# Step 4 : Release the app
sudo heroku container:release web
echo 'Done'
