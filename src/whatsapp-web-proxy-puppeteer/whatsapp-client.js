"use strict";

const { Client, LocalAuth } = require("whatsapp-web.js");
const qrcode = require("qrcode-terminal");
const {
  buildHttpPayloadFromClientEvent,
  buildHttpPayloadFromMessage,
  forwardMessagePayloadToHttp,
} = require("./utils");

/**
 * Create and start a whatsapp-web.js Client with basic authentication
 * and minimal event handlers. Also augments the client instance with
 * a helper method `sendMessageWithPayload` that wraps the library's
 * `sendMessage` and returns our HTTP-friendly payload.
 */
function createWhatsappClient(options = {}) {
  const client = new Client({
    authStrategy: new LocalAuth({
      clientId: options.clientId || "default",
    }),
    puppeteer: {
      executablePath: process.env.CHROME_PATH || undefined,
      headless: options.headless !== false,
      args: options.puppeteerArgs || [
        "--no-sandbox",
        "--disable-setuid-sandbox",
      ],
    },
    ...options.clientOptions,
  });

  client.on("qr", (qr) => {
    console.log("[whatsapp-web-proxy] QR code received");
    // Print QR code to terminal for authentication.
    qrcode.generate(qr, { small: true });
    if (options.onQr) {
      options.onQr(qr);
    }
  });

  client.on("ready", () => {
    const payload = buildHttpPayloadFromClientEvent("ready", {
      status: "connected",
    });
    console.log("[whatsapp-web-proxy] Client is ready");
    if (options.onReady) {
      options.onReady(payload);
    }
  });

  client.on("authenticated", (session) => {
    const payload = buildHttpPayloadFromClientEvent("authenticated", {
      session,
    });
    console.log("[whatsapp-web-proxy] Client authenticated");
    if (options.onAuthenticated) {
      options.onAuthenticated(payload);
    }
  });

  client.on("auth_failure", (msg) => {
    const payload = buildHttpPayloadFromClientEvent("auth_failure", {
      message: msg,
    });
    console.log("[whatsapp-web-proxy] Authentication failure:", msg);
    if (options.onAuthFailure) {
      options.onAuthFailure(payload);
    }
  });

  client.on("disconnected", (reason) => {
    const payload = buildHttpPayloadFromClientEvent("disconnected", {
      reason,
    });
    console.log("[whatsapp-web-proxy] Client disconnected:", reason);
    if (options.onDisconnected) {
      options.onDisconnected(payload);
    }
  });

  client.on("message", (message) => {
    const payload = buildHttpPayloadFromMessage(message);
    console.log("[whatsapp-web-proxy] Message received from", message.from, "at", new Date().toLocaleString('pt-BR', { timeZone: 'America/Sao_Paulo' }));
    if (options.onMessage) {
      options.onMessage(payload, message);
    }

    // Forward message payload via HTTP (fire-and-forget).
    forwardMessagePayloadToHttp(payload, options);
  });

  client.on("message_create", (message) => {
    if (message.fromMe) {
      const payload = buildHttpPayloadFromMessage(message);
      console.log("[whatsapp-web-proxy] Message sent to", message.to, "at", new Date().toLocaleString('pt-BR', { timeZone: 'America/Sao_Paulo' }));
      if (options.onMessageCreate) {
        options.onMessageCreate(payload, message);
      }

      // Forward message payload via HTTP (fire-and-forget).
      forwardMessagePayloadToHttp(payload, options);
    }
  });

  // Attach helper: wraps underlying sendMessage and returns HTTP payload.
  client.sendMessageWithPayload = async (chatId, content, sendOptions = {}) => {
    const message = await client.sendMessage(chatId, content, sendOptions);
    return buildHttpPayloadFromMessage(message);
  };

  client.initialize();
  return client;
}

module.exports = {
  createWhatsappClient,
};

