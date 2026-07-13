package com.equipment.management.common.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditJsonUtils {

    private static final String[] SENSITIVE_FIELDS = {
            "password", "oldPassword", "newPassword", "token", "accessToken", "refreshToken"
    };

    private final ObjectMapper objectMapper;

    public String toAuditJson(Object source) {
        if (source == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.valueToTree(source);
            maskSensitiveFields(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            return "{\"message\":\"" + ex.getMessage() + "\"}";
        }
    }

    public String buildResultJson(Object requestBody, boolean success, String errorMessage) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("success", success);
            if (errorMessage != null) {
                root.put("errorMessage", errorMessage);
            }
            if (requestBody != null) {
                JsonNode payload = objectMapper.valueToTree(requestBody);
                maskSensitiveFields(payload);
                root.set("payload", payload);
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            return "{\"success\":" + success + "}";
        }
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            for (String field : SENSITIVE_FIELDS) {
                if (objectNode.has(field)) {
                    objectNode.put(field, "******");
                }
            }
            objectNode.fields().forEachRemaining(entry -> maskSensitiveFields(entry.getValue()));
            return;
        }
        if (node.isArray()) {
            node.forEach(this::maskSensitiveFields);
        }
    }
}
