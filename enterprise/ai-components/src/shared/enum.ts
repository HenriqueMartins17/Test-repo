export enum AIType {
  Qa = 'qa',
  Chat = 'chat',
}

export enum TrainingStatus {
  FAILED = 'failed',
  NEW = 'new',
  TRAINING = 'training',
  SUCCESS = 'success',
}

export enum IAIMode {
  Wizard = 'wizard',
  Advanced = 'advanced',
}

export enum AIFeedbackType {
  DisLike = 0,
  Like = 1,
}

export enum AIFeedbackState {
  Processed = 1,
  Unprocessed = 0,
  Ignore = 2,
}

export enum BillingErrorCode {
  OVER_LIMIT = 1504,
}

export enum ScreenSize {
  xs = 'xs', // 0
  sm = 'sm', // 576
  md = 'md', // 768
  lg = 'lg', // 992
  xl = 'xl', // 1200
  xxl = 'xxl', // 1440
  xxxl = 'xxxl', // 1600
}
