"use strict";

const express = require("express");

/**
 * Starts an HTTP controller that accepts POST requests
 * with a body containing the number and message to send.
 *
 * Expected JSON body:
 * {
 *   "number": "5511999999999",
 *   "message": "Your text here"
 * }
 */
function startHttpController(client, options = {}) {
  const app = express();
  app.use(express.json());

  app.post("/send-message", async (req, res) => {
    const { number, message } = req.body || {};

    if (!number || !message) {
      return res.status(400).json({
        error: "Missing 'number' or 'message' in request body",
      });
    }

    try {
      if (typeof client.sendMessageWithPayload !== "function") {
        throw new Error("Client does not implement sendMessageWithPayload");
      }

      const payload = await client.sendMessageWithPayload(number, message);
      return res.status(200).json({
        status: "sent",
        payload,
      });
    } catch (err) {
      console.error("[whatsapp-web-proxy2] Error sending message:", err);
      return res.status(500).json({
        error: "Failed to send message",
      });
    }
  });

  const port = options.port || 8081;

  app.listen(port, () => {
    console.log(
      `[whatsapp-web-proxy2] HTTP controller listening on port ${port}`
    );
  });
}

module.exports = {
  startHttpController,
};

