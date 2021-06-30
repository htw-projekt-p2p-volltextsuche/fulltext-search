# syntax=docker/dockerfile:1

FROM mozilla/sbt

WORKDIR /app

COPY . .

CMD ["sbt", "run"]