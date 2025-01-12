const path = require('path');
const { merge } = require('webpack-merge');
const webpack = require('webpack');
const common = require('./webpack.base.js');
const proxy = require('./proxy.config.js');
const { swcLoaders } = require('./config');
const ReactRefreshWebpackPlugin = require('@pmmmwh/react-refresh-webpack-plugin');
const ErrorOverlayPlugin = require('error-overlay-webpack-plugin')

module.exports = merge(common, {
  mode: 'development',
  target: 'web',
  devtool: 'cheap-module-source-map',
  module: {
    rules: [
      ...swcLoaders(),
    ]
  },
  devServer: {
    // static: {
    //   directory: path.join(__dirname, '..', 'src')
    // },
    client: { overlay: false },
    historyApiFallback: true,
    hot: true,
    open: true,
    proxy: proxy,
    host: '0.0.0.0',
    compress: false,
  },
  // To disable compression :
  plugins: [
    new ErrorOverlayPlugin(),
    // new webpack.DefinePlugin({
    //   'process.env': {
    //     targetPlatform: JSON.stringify('web'),
    //   },
    // }),
    new ReactRefreshWebpackPlugin({ overlay: false }),
    // new webpack.HotModuleReplacementPlugin(),
    new webpack.ProvidePlugin({ React: 'react' }),
    new webpack.DefinePlugin({ TEST: '' }),
  ],
  cache: true,
});
