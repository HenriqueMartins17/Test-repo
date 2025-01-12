
enum StatusEnum {
  RUNNING,
  SUCCESS,
  ERROR
}
export const convertStatus2Code = (status: 'success' | 'failed' | 'training') => {
  if (status === 'success') return StatusEnum.SUCCESS;
  if (status === 'failed') return StatusEnum.ERROR;
  return StatusEnum.RUNNING;
};
