"use strict";

const { createWhatsappClient } = require("./index");
const { startHttpController } = require("./controller");

async function main() {
  try {
    console.log(
      "[whatsapp-web-proxy2] Bootstrapping WhatsApp (Baileys) client..."
    );
    const client = await createWhatsappClient();

    console.log("[whatsapp-web-proxy2] Starting HTTP controller...");
    startHttpController(client);
  } catch (err) {
    console.error("[whatsapp-web-proxy2] Fatal error while starting:", err);
    process.exit(1);
  }
}

main();

