package io.vertx.greet;

import java.time.Instant; 
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;


public class GreetServer {

    private int counter;
    private float maxRequestsPerSecond;
    private String greeting;
    private Instant lastRequestInstant;
    private Vertx vertx;

    GreetServer(String greeting, float maxRequestsPerSecond) {
        this.greeting = greeting;
        this.maxRequestsPerSecond = maxRequestsPerSecond;
        lastRequestInstant = Instant.MIN;
        vertx = Vertx.vertx();
    }

    public void start() {
        vertx
            .createHttpServer()
            .requestHandler(defineRoutes())
            .listen(8080);
    }

    private Router defineRoutes() {
        Router router = Router.router(vertx);

        router.get("/").handler(req -> {
            Instant now = Instant.now(); 
            if (isRequestWithinRateLimits(now)) {
                req.response().end(greeting + "\n");
            } else {
                req.response().setStatusCode(503).end();
            }

            lastRequestInstant = now;
            counter++;
        });

        router.get("/counter").handler(req ->
            req.response().end(counter + "\n")
        );

        return router;
    }

    private boolean isRequestWithinRateLimits(Instant instant) {
        if (maxRequestsPerSecond == 0) {
            return true;
        }

        long millis = (long) (1000 / maxRequestsPerSecond);
        return lastRequestInstant.plusMillis(millis).isBefore(instant);
    }

}