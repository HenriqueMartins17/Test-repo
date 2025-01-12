import {Typography} from "@apitable/components";
import dayjs from 'dayjs';
import React from "react";

interface ITimeItemProps{
  time: number;
}
export const TimeItem:React.FC<ITimeItemProps> = ({time}) => {
  return <Typography variant={'body3'}>
    {dayjs(time).format('YYYY-MM-DD HH:mm:ss')}
  </Typography>
};
