package com.mbu.dslibrary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Turns on automatic createdAt / updatedAt filling for documents. */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
