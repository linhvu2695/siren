# Siren: Mp3 converter

## Architecture

### 1. Auth
- `mysql -uroot < init.sql` init MySQL database and create server authentication
- `docker build -t siren-auth .` build Docker image 
- `kubectl apply -f ./manifests` deploy auth microservice

### 2. API Gateway
#### Packages
- `pika`: Python implementation of AMQP 0-9-1 prototcol (similar to Rabitmq architecture)
- `gridfs`: divide a large file (>16MB) into smaller chunks for better MongoDB management
#### Initialize service
- `docker build -t siren-gateway gateway/` build Docker image
- `kubectl apply -f rabbitmq/manifests` deploy rabbitmq microservice
- `kubectl apply -f gateway/manifests/` deploy gateway microservice
- `sudo vim /etc/hosts` open host file and add the `127.0.0.1 mp3converter.com` to EOF
- `minikube addons enable ingress` enable ingress
- `minikube tunnel` start k8s tunnel to enable domain mp3converter.com to connect to the cluster
#### MongoDB 
- `cd gateway/storage/ && docker-compose up -d` start MongoDB instance

### 3. RabbitMQ
- `sudo vim /etc/hosts` open host file and add the `127.0.0.1 rabbitmq-manager.com` to EOF
- Login using `guest:guest` credentials
- Add 2 queues with the name `video` and `mp3`
- `TODO`: in case of rabbitmq pod failure, a new pod will be initiated and rabbitmq-service will update its IP address, but `gateway` will still refer to the previous version of rabbitmq-service -> result in `Channel is closed` error -> need to refresh gateway

### 4 Converter
- `docker build -t siren-converter converter/` build Docker image
- `kubectl apply -f converter/manifests` deploy converter microservice

### 5. Notification
- `docker build -t siren-notification notification/` build Docker image
- `kubectl apply -f notification/manifests` deploy converter microservice

## Usage
- `curl -X POST http://mp3converter.com/login -u linhvu2695@gmail.com:password` get token
- `curl -X POST http://mp3converter.com/upload -F "file=@./test.mp4" -H 'Auhtorization: Bearer <token>'` upload file
- An email will be sent to your username email with mp3_fid
- `curl -X GET "http://mp3converter.com/download?fid=<mp3_fid>" --output test.mp3 -H 'Authorization: Bearer` download file

