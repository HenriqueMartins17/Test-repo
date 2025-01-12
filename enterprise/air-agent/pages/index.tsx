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

import Link from 'next/link';
import LandingHeader from 'components/landing/header';

const App = () => {
  return (
    <>
      <LandingHeader />
      <main className="vk-flex vk-flex-col vk-justify-between vk-items-center vk-space-y-30">
        <div className="vk-my-20 vk-inline-flex vk-flex-col vk-items-center vk-space-y-10">
          <p className="vk-text-5xl vk-font-bold">
            Turns{' '}
            <span className="vk-bg-gradient-to-r vk-from-indigo-500 vk-via-purple-500 vk-to-pink-500 vk-gradient-flow vk-bg-clip-text vk-font-semibold vk-leading-none vk-text-transparent">
              Airtable
            </span>{' '}
            data into AI Agent in 1-click
          </p>
          <div>
            <Link
              href={'/workspace'}
              passHref
              className="vk-rounded-xl vk-p-3 vk-border-2 vk-shadow-lg vk-border-purple-200 hover:bg-purple-100 vk-transition vk-ease-in-out vk-duration-300"
            >
              Get Start Now
            </Link>
          </div>
        </div>
      </main>
    </>
  );
};

// export const getServerSideProps = async(context: NextPageContext) => {
// axios.defaults.baseURL = getBaseUrl(context);

// if (!context.req?.url) {
//   return { props: {}};
// }
// const cookie = context.req?.headers.cookie;
// const headers: Record<string, string> = {};

// if (cookie) {
//   headers.cookie = cookie;
// }

// const spaceId = context.query?.spaceId || '';
// const res = await axios.get('/client/info', { params: { spaceId }, headers: headers });

// const userInfo = res.data.userInfo;

// if (!userInfo || userInfo === 'null') {
//   return {
//     redirect: {
//       destination: '/login',
//       statusCode: 302,
//     },
//   };
// }
// return { props: {}};
// };

export const config = {
  unstable_includeFiles: ['../../node_modules/next/dist/compiled/@edge-runtime/primitives/**/*.+(js|json)'],
};

export default App;
