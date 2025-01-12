import axios from 'axios';

export const getHistory = (path: any) => {
  return axios.get(path).then((res) => res.data.data);
};
