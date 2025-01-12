const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpack = require('webpack');
const WebpackBar = require('webpackbar');
const package = require('../package.json');
const { styleLoaders, scriptLoaders } = require('./config');

const { fullPath, assetsPath } = require('./utils');

const isProd = process.env.NODE_ENV === 'production';

module.exports = {
  entry: fullPath('src/index.tsx'),
  plugins: [
    new MiniCssExtractPlugin({
      filename: assetsPath('css/[name].[chunkhash].css'),
      ignoreOrder: true,
    }),
    new webpack.DefinePlugin({
      'process.env': {
        // NODE_ENV: JSON.stringify(process.env.NODE_ENV),
      },
    }),
    new HtmlWebpackPlugin({
      template: './src/index.html',
      inject: 'body',
    }),
    new WebpackBar({
      name: 'AI Widget',
      color: '#00C5A6',
    }),
  ],
  module: {
    rules: [
      ...styleLoaders({
        extract: isProd,
      }),
      {
        test: /\.(png|jpg|gif)$/i,
        type: 'asset/resource',
      },
      {
        test: /\.(woff2?|eot|ttf|otf|svg)(\?.*)?$/,
        type: 'asset/resource',
      },
      {
        test: /\.txt$/,
        type: 'asset/source',
      },
      { test: /\.json$/, type: 'json' }
    ],
  },
  resolve: {
    fallback: {
      path: false,
      fs: false,
      process: false,
      util: false,
    },
    extensions: ['.js', '.jsx', '.ts', '.tsx', '.json', '.less'],
    alias: {
      '@': fullPath('src'),
      // ':': fullPath('src/components'),
    },
  },
  output: {
    path: fullPath('dist'),
    publicPath: '/',
    filename: assetsPath('js/[name].[chunkhash].js'),
    chunkFilename: assetsPath('js/[name].[chunkhash].js'),
    assetModuleFilename: assetsPath('assets/[name].[hash][ext][query]'),
    clean: true,
  },
  optimization: {
    moduleIds: 'deterministic',
    // minimizer,
  },

  experiments: {
    asyncWebAssembly: true, // 这个项目循环依赖实在太多了 暂时改不动 wasm 暂时没法用了
    // buildHttp: true,
    // executeModule: true,
    // layers: true,
    // lazyCompilation: true,
    // outputModule: true,
    // syncWebAssembly: true,
    topLevelAwait: true,
  },
};
