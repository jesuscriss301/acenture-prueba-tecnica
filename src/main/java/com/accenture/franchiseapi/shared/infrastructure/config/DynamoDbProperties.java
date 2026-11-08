package com.accenture.franchiseapi.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param tableName nombre de la tabla única
 * @param region    región de AWS
 * @param endpoint  opcional: URL de DynamoDB Local (vacío = AWS real)
 */
@ConfigurationProperties(prefix = "app.dynamodb")
public record DynamoDbProperties(String tableName, String region, String endpoint) {
}
