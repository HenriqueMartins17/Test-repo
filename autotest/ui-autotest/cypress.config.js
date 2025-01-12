const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    "baseUrl": "https://vika.cn",
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
