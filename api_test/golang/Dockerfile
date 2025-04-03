FROM golang:1.24.1 AS builder
WORKDIR /app
COPY . .
RUN CGO_ENABLED=0 go build -o main

FROM scratch
COPY --from=builder /app/main /main
COPY --from=builder /app/queries /queries
COPY --from=builder /app/expected_responses /expected_responses
ENTRYPOINT ["/main"]