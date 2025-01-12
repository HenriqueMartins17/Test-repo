/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import type { AppProps } from 'next/app';
import Head from 'next/head';
import 'normalize.css';
import 'styles/index.less';
import 'styles/global.less';
import { GlobalProvider } from 'context/global';
import { getInitialProps } from '../utils/get_initial_props';

function App(props: AppProps & { envVars: string }) {
  const { Component, pageProps } = props;

  return (
    <>
      <Head>
        <title>AirAgent</title>
        <meta name="description" content="AirAgent" />
        <meta name="renderer" content="webkit" />
        <meta
          name="viewport"
          content="width=device-width,viewport-fit=cover, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"
        />
        <meta name="theme-color" content="#000000" />
      </Head>
      <div className={'__next_main'}>
        <GlobalProvider>
          <Component {...pageProps} />
        </GlobalProvider>
      </div>
    </>
  );
}

export default App;

App.getInitialProps = getInitialProps;
