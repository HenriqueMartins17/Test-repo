import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';

export interface IAIServerResponse<T = any> {
  success: boolean;
  message: string;
  code: number;
  data: T;
}

const onResponseError = (error: AxiosError<IAIServerResponse>) => {
  if (error.isAxiosError) {
    const { response } = error;
    if (response) {
      const { data } = response;
      const { message } = data;
      if (message) {
        error.message = message;
      }
    }
  }
  throw error;
};

class CustomError extends Error {
  code!: number;

  constructor(message: string) {
    super(message);
    this.name = 'CustomError';
  }
}

const onResponse = (response: AxiosResponse<IAIServerResponse>) => {
  if (response.data.success === false) {
    const error = new CustomError(response.data.message);
    error.code = response.data.code;
    throw error;
  } else {
    return response.data;
  }
};
export const createAxios = (config: AxiosRequestConfig) => {
  const http = axios.create(
    Object.assign(
      {
        timeout: 5 * 1000,
        withCredentials: true,
      },
      config,
    ),
  );
  http.interceptors.response.use(onResponse, onResponseError);
  return http;
};

export const http = createAxios({
  baseURL: '/api/v1',
});
