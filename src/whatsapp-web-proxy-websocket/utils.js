"use strict";

/**
 * Safely convert objects coming from Baileys into JSON
 * that can be sent via HTTP (no functions, no circular references).
 */
function serializeForHttp(obj) {
  const seen = new WeakSet();

  return JSON.parse(
    JSON.stringify(obj, (key, value) => {
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
    })
  );
}

function extractTextFromMessageContent(messageContent) {
  if (!messageContent) return "";

  if (messageContent.conversation) {
    return messageContent.conversation;
  }

  if (
    messageContent.extendedTextMessage &&
    messageContent.extendedTextMessage.text
  ) {
    return messageContent.extendedTextMessage.text;
  }

  if (messageContent.imageMessage && messageContent.imageMessage.caption) {
    return messageContent.imageMessage.caption;
  }

  if (messageContent.videoMessage && messageContent.videoMessage.caption) {
    return messageContent.videoMessage.caption;
  }

  return "";
}

/**
 * Build a generic HTTP payload from a Baileys WAMessage.
 * This is intentionally generic and does not send anything itself.
 */
function buildHttpPayloadFromMessage(message, extra = {}) {
  const text = extractTextFromMessageContent(message.message);

  const raw = {
    id: message.key && message.key.id,
    from: message.key && message.key.remoteJid,
    fromMe: message.key && message.key.fromMe,
    pushName: message.pushName,
    timestamp: message.messageTimestamp,
    body: text,
    ...extra,
  };

  return serializeForHttp(message);
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
  const url = options.forwardUrl || "http://localhost:8080/whatsapp";

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
        "[whatsapp-web-proxy2] Error forwarding message via HTTP:",
        err
      );
    }
  })();
}

/**
 * Normalize a phone number into a WhatsApp JID for Baileys.
 * Examples:
 *   "5511999999999" -> "5511999999999@s.whatsapp.net"
 *   "5511999999999@s.whatsapp.net" -> unchanged
 */
function normalizeNumberToJid(number) {
  if (!number) return number;

  const trimmed = String(number).trim();

  if (
    trimmed.endsWith("@s.whatsapp.net") ||
    trimmed.endsWith("@g.us")
  ) {
    return trimmed;
  }

  const digits = trimmed.replace(/\D/g, "");
  if (!digits) return trimmed;

  return `${digits}@s.whatsapp.net`;
}

module.exports = {
  serializeForHttp,
  buildHttpPayloadFromMessage,
  buildHttpPayloadFromClientEvent,
  forwardMessagePayloadToHttp,
  normalizeNumberToJid,
};

