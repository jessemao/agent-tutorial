ARG MAVEN_IMAGE=public.ecr.aws/docker/library/maven:3.9.9-eclipse-temurin-17
ARG COURSE_REF=s2-t01-start
ARG COURSE_COMMIT=unknown

# Use a disposable copy of the project to resolve every build/test dependency.
# The final image receives only the Maven repository, never the training source
# or answer tags.
FROM ${MAVEN_IMAGE} AS dependency-cache
WORKDIR /seed

COPY pom.xml ./
COPY platform-contracts/pom.xml platform-contracts/pom.xml
COPY platform-web-starter/pom.xml platform-web-starter/pom.xml
COPY business-wms/pom.xml business-wms/pom.xml
COPY training-server/pom.xml training-server/pom.xml
COPY docker/classroom/settings.xml /seed/settings.xml

COPY platform-contracts/src platform-contracts/src
COPY platform-web-starter/src platform-web-starter/src
COPY business-wms/src business-wms/src
COPY training-server/src training-server/src

# Run and install the intentionally failing course starting version while warming all
# test providers and runtime dependencies. T01's expected test failures are
# recorded but do not abort image creation; learners reproduce them offline.
RUN mvn -s /seed/settings.xml -B -ntp \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.test.failure.ignore=true clean install \
    && find /seed -type d -name target -prune -exec rm -rf '{}' + \
    && rm -f /seed/settings.xml

FROM ${MAVEN_IMAGE} AS classroom
ARG COURSE_REF
ARG COURSE_COMMIT

LABEL org.opencontainers.image.title="Training WMS Classroom Environment" \
      org.opencontainers.image.description="Java 17, Maven and pre-warmed dependencies for the AI Coding WMS course" \
      org.opencontainers.image.version="0.7" \
      io.training.course.ref="${COURSE_REF}" \
      org.opencontainers.image.revision="${COURSE_COMMIT}"

ENV MAVEN_OPTS="-Dmaven.repo.local=/opt/training-m2/repository" \
    MAVEN_ARGS="-s /opt/classroom/settings.xml" \
    MAVEN_CONFIG="/workspace/.classroom-runtime" \
    HOME="/workspace/.classroom-runtime" \
    TMPDIR="/workspace/.classroom-runtime" \
    JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/workspace/.classroom-runtime"

COPY --from=dependency-cache /root/.m2/repository /opt/training-m2/repository
COPY --from=dependency-cache /seed /workspace
COPY docker/classroom/settings.xml /opt/classroom/settings.xml
COPY docker/classroom/training-wms-start /usr/local/bin/training-wms-start

RUN mkdir -p /workspace/.classroom-runtime \
    && chmod -R a+rX /opt/training-m2 \
    && chmod a+r /opt/classroom/settings.xml \
    && chmod a+rx /usr/local/bin/training-wms-start \
    && chmod 1777 /workspace/.classroom-runtime

WORKDIR /workspace
EXPOSE 8080

# The image is built from the immutable s2-t01-start course starting version. Its
# default action verifies that embedded starting code without network access.
CMD ["mvn", "-o", "-B", "-ntp", "clean", "verify"]
