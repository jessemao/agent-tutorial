ARG MAVEN_IMAGE=public.ecr.aws/docker/library/maven:3.9.9-eclipse-temurin-17
ARG NODE_IMAGE=node:22-bookworm-slim
ARG COURSE_REF=s2-t01-start
ARG COURSE_COMMIT=unknown

FROM ${NODE_IMAGE} AS node-runtime

FROM node-runtime AS frontend-dependency-cache
WORKDIR /frontend
COPY training-server/frontend/package.json training-server/frontend/package-lock.json ./
RUN npm ci --ignore-scripts

# Use a disposable copy of the project to resolve every build/test dependency.
# The final image receives only the Maven repository, never the training source
# or answer tags.
FROM ${MAVEN_IMAGE} AS dependency-cache
WORKDIR /seed

# Reuse a pre-warmed classroom image when it is supplied as MAVEN_IMAGE for
# local/offline rebuilds; the official Maven base simply skips this copy.
RUN mkdir -p /cache/repository \
    && if [ -d /opt/training-m2/repository ]; then \
         cp -a /opt/training-m2/repository/. /cache/repository/; \
       fi

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
    -Dmaven.repo.local=/cache/repository \
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

COPY --from=dependency-cache /cache/repository /opt/training-m2/repository
COPY --from=dependency-cache /seed /workspace
COPY --from=node-runtime /usr/local/bin/node /usr/local/bin/node
COPY --from=node-runtime /usr/local/lib/node_modules /usr/local/lib/node_modules
COPY --from=frontend-dependency-cache /frontend/node_modules /opt/training-node-modules
COPY docker/classroom/settings.xml /opt/classroom/settings.xml
COPY docker/classroom/training-wms-start /usr/local/bin/training-wms-start
COPY docker/classroom/training-wms-dev /usr/local/bin/training-wms-dev
COPY docker/classroom/training-wms-frontend-dev /usr/local/bin/training-wms-frontend-dev
COPY docker/classroom/training-wms-mvn /usr/local/bin/training-wms-mvn

RUN mkdir -p /workspace/.classroom-runtime \
    && ln -s ../lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm \
    && ln -s ../lib/node_modules/npm/bin/npx-cli.js /usr/local/bin/npx \
    && chmod -R a+rX /opt/training-m2 \
    && chmod -R a+rX /opt/training-node-modules \
    && chmod a+r /opt/classroom/settings.xml \
    && chmod a+rx /usr/local/bin/training-wms-start /usr/local/bin/training-wms-dev /usr/local/bin/training-wms-frontend-dev /usr/local/bin/training-wms-mvn \
    && chmod 1777 /workspace/.classroom-runtime

WORKDIR /workspace
EXPOSE 8080

# The image is built from the immutable s2-t01-start course starting version. Its
# default action verifies that embedded starting code without network access.
CMD ["mvn", "-o", "-B", "-ntp", "clean", "verify"]
