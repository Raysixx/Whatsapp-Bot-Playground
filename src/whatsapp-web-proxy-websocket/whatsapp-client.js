"use strict";

const baileys = require("@whiskeysockets/baileys");
const makeWASocket = baileys.default;
const { useMultiFileAuthState, fetchLatestBaileysVersion } = baileys;
const qrcode = require("qrcode-terminal");

const {
  buildHttpPayloadFromClientEvent,
  buildHttpPayloadFromMessage,
  forwardMessagePayloadToHttp,
  normalizeNumberToJid,
  serializeForHttp
} = require("./utils");

/**
 * Create and start a Baileys WhatsApp client with basic authentication
 * and minimal event handlers. Also augments the client instance with
 * a helper method `sendMessageWithPayload` that wraps the library's
 * `sendMessage` and returns our HTTP-friendly payload.
 */
async function createWhatsappClient(options = {}) {
  const authDir = options.authDir || "./.baileys_auth";

  const { state, saveCreds } = await useMultiFileAuthState(authDir);
  const { version } = await fetchLatestBaileysVersion();

  const sock = makeWASocket({
    version,
    auth: state,
    printQRInTerminal: false, // we print via qrcode-terminal (same as whatsapp-web-proxy)
  });

  sock.ev.on("creds.update", saveCreds);

  let connectedAt = null;

  sock.ev.on("connection.update", (update) => {
    const { connection, lastDisconnect, qr } = update;

    if (qr) {
      console.log("[whatsapp-web-proxy2] QR code received");
      // Print QR code to terminal for authentication (same as whatsapp-web-proxy).
      qrcode.generate(qr, { small: true });
      if (options.onQr) {
        options.onQr(qr);
      }
    }

    if (connection === "open") {
      connectedAt = Math.floor(Date.now() / 1000);

      const payload = buildHttpPayloadFromClientEvent("ready", {
        status: "connected",
      });
      console.log("[whatsapp-web-proxy2] Client is ready");
      if (options.onReady) {
        options.onReady(payload);
      }
    } else if (connection === "close") {
      const statusCode = lastDisconnect?.error?.output?.statusCode;

      const payload = buildHttpPayloadFromClientEvent("disconnected", {
        reason:
          lastDisconnect &&
          lastDisconnect.error &&
          (lastDisconnect.error.output ||
            lastDisconnect.error.message ||
            String(lastDisconnect.error)),
      });
      console.log("[whatsapp-web-proxy2] Client disconnected, code:", statusCode);

      if (options.onDisconnected) {
        options.onDisconnected(payload);
      }

      const shouldReconnect = statusCode !== DisconnectReason.loggedOut;

      if (shouldReconnect) {
        console.log("[whatsapp-web-proxy2] Reconnecting...");
        setTimeout(() => createWhatsappClient(options), 3000);
      } else {
        console.log("[whatsapp-web-proxy2] Logged out. Scan QR code again.");
      }
    }
  });

  sock.ev.on("messages.upsert", (m) => {
    m.messages.forEach(msg => {
      if (!msg) return;

      const msgTimestamp = typeof msg.messageTimestamp === 'object'
      ? msg.messageTimestamp.low
      : msg.messageTimestamp;

      if (connectedAt && msgTimestamp < connectedAt - 30) return;

      const fromMe = !!(msg.key && msg.key.fromMe);

      if (typeof msg.messageTimestamp === 'object') {
        msg.messageTimestamp = msg.messageTimestamp.low
      }

      const messageForPayload = {
        ...msg,
        meId: sock.user.id,
        meLid: sock.user.lid
      }

      const payload = serializeForHttp(messageForPayload);

      const when = new Date().toLocaleString("pt-BR", {
        timeZone: "America/Sao_Paulo",
      });

      if (fromMe) {
        console.log(
          "[whatsapp-web-proxy2] Message sent to",
          msg.key && msg.key.remoteJid,
          "at",
          when
        );
        if (options.onMessageCreate) {
          options.onMessageCreate(payload, msg);
        }
      } else {
        console.log(
          "[whatsapp-web-proxy2] Message received from",
          msg.key && msg.key.remoteJid,
          "at",
          when
        );
        if (options.onMessage) {
          options.onMessage(payload, msg);
        }
      }

      // Forward message payload via HTTP (fire-and-forget).
      forwardMessagePayloadToHttp(payload, options);
    });
  });

  // Attach helper: wraps underlying sendMessage and returns HTTP payload.
  sock.sendMessageWithPayload = async (number, content, sendOptions = {}) => {
    const message = await sock.sendMessage(
      number,
      { text: content },
      sendOptions
    );
    return serializeForHttp(message);
  };

  return sock;
}

module.exports = {
  createWhatsappClient,
};

