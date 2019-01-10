FROM golang:1.9-alpine as builder

# Metadata
LABEL module.maintainer "onesaitplatform@indra.es" \
      module.name="burrow"

# Default hostname
ENV SERVER_NAME=localhost \
    ZOOKEEPER_SERVER=localhost
      
ENV DEP_VERSION="0.3.2"
RUN echo -e "http://nl.alpinelinux.org/alpine/v3.6/main" > /etc/apk/repositories && \
    echo -e "http://nl.alpinelinux.org/alpine/v3.6/community" >> /etc/apk/repositories
RUN apk add --no-cache git curl && \
	curl -L -s https://github.com/golang/dep/releases/download/v${DEP_VERSION}/dep-linux-amd64 -o $GOPATH/bin/dep && \
	chmod +x $GOPATH/bin/dep && \
	mkdir -p $GOPATH/src/github.com/linkedin/Burrow

ADD . $GOPATH/src/github.com/linkedin/Burrow/
RUN cd $GOPATH/src/github.com/linkedin/Burrow && \
	dep ensure && \
	go build -o /tmp/burrow .

FROM iron/go
MAINTAINER LinkedIn Burrow "https://github.com/linkedin/Burrow"

WORKDIR /app
COPY --from=builder /tmp/burrow /app/
ADD /docker-config/* /etc/burrow/

EXPOSE 18400

ENTRYPOINT ["/app/burrow", "--config-dir", "/etc/burrow"]
