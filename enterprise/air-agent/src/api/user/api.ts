import { http } from 'api';
import { IUserProfile } from './types';

export const getUserInfo = () => {
  return http.get<IUserProfile>('/airagent/user/profile');
};

export const signOut = () => {
  return http.get('/airagent/logout');
};
