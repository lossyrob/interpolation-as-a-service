IMG  := quay.io/lossyrob/interpolation-as-a-service
TAG  := "latest"

ASSEMBLY_JAR := server/target/scala-2.11/interpolation-as-a-service-*.jar

clean:
	rm -rf ${ASSEMBLY_JAR}
	rm ./build
	rm ./assemble

rwildcard=$(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

${ASSEMBLY_JAR}: $(call rwildcard, src, *.scala) build.sbt
	./sbt assembly

assembly: ${ASSEMBLY_JAR}

build: Dockerfile assembly
	docker build -t ${IMG}:${TAG}	.

publish: build
	docker push ${IMG}:${TAG}

test: build
	./sbt test
	docker-compose up -d
	sleep 2 && curl localhost:7070/system/status
	docker-compose down
