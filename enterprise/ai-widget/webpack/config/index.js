const loaders = require('./loaders');
const minimizer = require('./minimizer');

module.exports = {
  minimizer,
  styleLoaders: loaders.styleLoaders,
  scriptLoaders: loaders.scriptLoaders,
  swcLoaders: loaders.swcLoaders,
};
