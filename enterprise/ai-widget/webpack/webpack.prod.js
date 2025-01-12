// const webpack = require('webpack');
const { merge } = require('webpack-merge');
const common = require('./webpack.base.js');
const { minimizer, swcLoaders } = require('./config');
const { assetsPath } = require('./utils');
// const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = merge(common, {
  mode: 'production',
  entry: {
    index: './src/index.tsx',
  },
  module: {
    rules: [
      ...swcLoaders(),
    ]
  },
  plugins: [
    // new MiniCssExtractPlugin({
    //   filename: assetsPath('css/[name].css'),
    //   ignoreOrder: true,
    // }),
    // new webpack.DefinePlugin({
    //   'process.env': {
    //     targetPlatform: JSON.stringify('web'),
    //   },
    // }),
  ],
  optimization: {
    minimizer,
    // removeEmptyChunks: true,
    // sideEffects: true,
    // mergeDuplicateChunks: true,
    // runtimeChunk: {
    //   name: assetsPath('js/runtime'),
    //   // filename: assetsPath('js/runtime')
    // },
    // moduleIds: 'deterministic',

    // splitChunks,
  },
});
