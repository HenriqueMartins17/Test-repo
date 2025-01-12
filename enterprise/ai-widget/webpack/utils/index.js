const path = require('path');

const fullPath = (s) => path.join(__dirname, '..', '..', s);
const assetsPath = (s) => path.posix.join('static', s || '');

module.exports = {
  fullPath,
  assetsPath,
};
