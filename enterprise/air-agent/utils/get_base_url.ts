import { NextPageContext } from 'next';
import { Url } from '@apitable/core';

export const getBaseUrl = (context: NextPageContext) => {
  const host = process.env.API_PROXY || `http://${context.req?.headers.host}` || '';
  return host + Url.BASE_URL;
};
