const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const { fullPath } = require('../utils');

const includes = [fullPath('config'), fullPath('src'), fullPath('report')];
const isProd = process.env.NODE_ENV === 'production';

const swcLoaders = () => [
  {
    test: /\.[jt]sx?$/,
    exclude: /node_modules/,
    include: [...includes, /@apitable/],
    use: [
      {
        loader: 'swc-loader',
        // options: {
        //   env: { mode: 'entry' },
        //   jsc: {
        //     parser: {
        //       syntax: 'typescript',
        //       tsx: true,
        //       dynamicImport: true,
        //     },
        //     transform: {
        //       react: {
        //         runtime: 'automatic',
        //         refresh: !isProd,
        //       },
        //     },
        //   },
        // },
      },
    ],
  },
];

const styleLoaders = (options = {}) => {
  const loaders = {
    less: {
      loader: 'less-loader',
      options: {
        lessOptions: {
          javascriptEnabled: true,
        },
      },
    },
  };

  const devLoader = options.extract
    ? {
      loader: MiniCssExtractPlugin.loader,
      options: {},
    }
    : 'style-loader';

  const cssRules = ['less', 'css', 'scss', 'styl', 'stylus'].map(
    (extension) => {
      const rule = {
        test: new RegExp(`\\.${extension}$`),
        // exclude: new RegExp(`\\.module.${extension}$`),
        use: [
          devLoader,
          {
            loader: 'css-loader',
            options: {
              importLoaders: 2,
              modules: {
                auto: true,
                localIdentName: '[path][name]__[local]--[hash:base64:5]',
                // localIdentContext: fullPath("src"),
              },
            },
          },
          'postcss-loader',
        ],
      };
      if (loaders[extension]) {
        rule.use.push(loaders[extension]);
      }
      return rule;
    },
  );
  return cssRules;
};

module.exports = {
  styleLoaders,
  swcLoaders,
};
