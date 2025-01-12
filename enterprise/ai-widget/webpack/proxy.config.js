const defaultOptions = {
  logLevel: 'debug',
  changeOrigin: true,
  cookieDomainRewrite: '',
};

module.exports = {
  '/api/': {
    target: 'https://integration.vika.ltd',
    ...defaultOptions,
  },
  '/nest/': {
    target: 'https://integration.vika.ltd',
    ...defaultOptions,
  },
  '/file': {
    target: 'https://integration.vika.ltd',
    ...defaultOptions,
  },
};
