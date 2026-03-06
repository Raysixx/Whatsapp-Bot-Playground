"use strict";

const { createWhatsappClient } = require("./index");
const { startHttpController } = require("./controller");

async function main() {
  console.log("[whatsapp-web-proxy] Bootstrapping WhatsApp client...");
  const client = createWhatsappClient();

  console.log("[whatsapp-web-proxy] Starting HTTP controller...");
  startHttpController(client);
}

main();

