"use strict";

/**
 * Safely convert objects coming from whatsapp-web.js into JSON
 * that can be sent via HTTP (no functions, no circular references).
 */
function serializeForHttp(obj) {
  const seen = new WeakSet();

  return JSON.parse(
    JSON.stringify(
      obj,
      (key, value) => {
        if (typeof value === "function") {
          return undefined;
        }
        if (typeof value === "object" && value !== null) {
          if (seen.has(value)) {
            return undefined;
          }
          seen.add(value);
        }
        return value;
      }
    )
  );
}

/**
 * Build a generic HTTP payload from a whatsapp-web.js message.
 * This is intentionally generic and does not send anything itself.
 */
function buildHttpPayloadFromMessage(message) {
  const raw = {
    id: message.id,
    notifyName: message._data.notifyName,
    from: message.from,
    to: message.to,
    author: message.author,
    timestamp: message.timestamp,
    body: message.body,
    type: message.type,
    fromMe: message.fromMe,
    hasMedia: message.hasMedia,
    deviceType: message.deviceType,
    isForwarded: message.isForwarded,
  };

  return serializeForHttp(raw);
}

/**
 * Example helper for building an HTTP payload for connection state.
 */
function buildHttpPayloadFromClientEvent(eventName, payload) {
  return serializeForHttp({
    event: eventName,
    payload,
    emittedAt: new Date().toISOString(),
  });
}

/**
 * Forward a prepared message payload to an HTTP endpoint.
 * The response is ignored; only errors are logged.
 */
function forwardMessagePayloadToHttp(payload, options = {}) {
  const url = "http://localhost:8080/whatsapp";

  (async () => {
    try {
      await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });
    } catch (err) {
      console.error(
        "[whatsapp-web-proxy] Error forwarding message via HTTP:",
        err
      );
    }
  })();
}

module.exports = {
  serializeForHttp,
  buildHttpPayloadFromMessage,
  buildHttpPayloadFromClientEvent,
  forwardMessagePayloadToHttp,
};

