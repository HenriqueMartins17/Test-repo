const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = [
  new TerserPlugin({
    minify: TerserPlugin.swcMinify,
    // `terserOptions` options will be passed to `swc` (`@swc/core`)
    // Link to options - https://swc.rs/docs/config-js-minify
    // terserOptions: {},
  }),
  new CssMinimizerPlugin({
    parallel: true,
  }),
];
