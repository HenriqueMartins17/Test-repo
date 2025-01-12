/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories
 * does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

import { DatasheetRecordAlarmService } from './datasheet.record.alarm.service';
import dayjs from 'dayjs';
import { DatasheetRecordAlarmBaseService } from 'database/alarm/datasheet.record.alarm.base.service';
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify';
import { Test, TestingModule } from '@nestjs/testing';
import { AppModule } from '../../../../app.module';

describe('datasheet record alarm service', () => {
  let app: NestFastifyApplication;
  let alarmService: DatasheetRecordAlarmService;

  beforeAll(async() => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();
    app = module.createNestApplication<NestFastifyApplication>(new FastifyAdapter());
    await app.init();
    alarmService = app.get(DatasheetRecordAlarmBaseService);
  });

  afterAll(async() => {
    await app.close();
  });

  describe('calculate record alarm at', () => {
    it('should returns origin input date value when alarmAtTime is invalid', () => {
      const nowTime = dayjs('2022-03-28T13:07:30Z');
      const alarmAtTime = 'xx:xx';
      const alarmAtSubtract = '';

      const alarmAt = alarmService.calculateAlarmAt(nowTime, alarmAtTime, alarmAtSubtract);
      expect(dayjs(alarmAt).diff(nowTime)).toEqual(0);
    });

    it('should returns origin input date value when alarmAtSubtract is invalid', () => {
      const nowTime = dayjs('2022-03-28T13:07:30Z');
      const alarmAtTime = '';
      const alarmAtSubtract = '5foo';

      const alarmAt = alarmService.calculateAlarmAt(nowTime, alarmAtTime, alarmAtSubtract);
      expect(dayjs(alarmAt).diff(nowTime)).toEqual(0);
    });

    it('should calculate alarm at with empty alarm at time and subtract', () => {
      const nowTime = dayjs('2022-03-28T13:07:30Z');
      const alarmAtTime = '';
      const alarmAtSubtract = '';

      const alarmAt = alarmService.calculateAlarmAt(nowTime, alarmAtTime, alarmAtSubtract);
      expect(dayjs(alarmAt).diff(nowTime)).toEqual(0);
    });

    // todo(wuchen): set utc timezone for jest
    // it('should calculate alarm at with alarm at time but empty subtract', () => {
    //   const nowTime = dayjs('2022-03-28T13:30:00Z');
    //   const alarmAtTime = '12:00';
    //   const alarmAtSubtract = '';

    //   const alarmAt = alarmService.calculateAlarmAt(nowTime, alarmAtTime, alarmAtSubtract);
    //   expect(dayjs(alarmAt).diff(nowTime, 'minute')).toEqual(-90);
    // });

    it('should calculate alarm at with subtract but empty alarm at time', () => {
      const nowTime = dayjs('2022-03-28T13:30:00Z');
      const alarmAtTime = '';
      const alarmAtSubtract = '5m';

      const alarmAt = alarmService.calculateAlarmAt(nowTime, alarmAtTime, alarmAtSubtract);
      expect(dayjs(alarmAt).diff(nowTime, 'minute')).toEqual(-5);
    });

    // todo(wuchen): set utc timezone for jest
    // it('should calculate alarm at with alarm at time and subtract', async() => {
    //   const nowTime = dayjs('2022-03-28T13:30:00Z');
    //   const alarmAtTime = '12:00';
    //   const alarmAtSubtract = '5m';

    //   const alarmAt = alarmService.calculateAlarmAt(nowTime, alarmAtTime, alarmAtSubtract);
    //   expect(dayjs(alarmAt).diff(nowTime, 'minute')).toEqual(-95);
    // });
  });

});
