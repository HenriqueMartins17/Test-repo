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

import { DatasheetEntity } from 'database/datasheet/entities/datasheet.entity';
import { DatasheetRecordEntity } from 'database/datasheet/entities/datasheet.record.entity';
import { UserEntity } from 'user/entities/user.entity';
import { DatasheetRecordRepository } from 'database/datasheet/repositories/datasheet.record.repository';
import { DatasheetRepository } from 'database/datasheet/repositories/datasheet.repository';
import { UserRepository } from 'user/repositories/user.repository';
import { DatasheetRecordSubscriptionBaseService } from 'database/subscription/datasheet.record.subscription.base.service';
import { DatasheetRecordSubscriptionRepository } from '../repositories/datasheet.record.subscription.repository';
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify';
import { Test, TestingModule } from '@nestjs/testing';
import { AppModule } from '../../../../app.module';
import { clearDatabase } from 'shared/testing/test-util';
import { getConnection } from 'typeorm';

describe('datasheet record subscription service', () => {
  let app: NestFastifyApplication;
  let datasheetRecordSubscriptionService: DatasheetRecordSubscriptionBaseService;
  let datasheetRecordSubscriptionRepo: DatasheetRecordSubscriptionRepository;
  let datasheetRecordRepo: DatasheetRecordRepository;
  let datasheetRepo: DatasheetRepository;
  let userRepo: UserRepository;
  let testUser: UserEntity;
  let testDst: DatasheetEntity;
  let testRecord1: DatasheetRecordEntity;
  let testRecord2: DatasheetRecordEntity;
  let testRecord3: DatasheetRecordEntity;

  beforeAll(async() => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();
    app = module.createNestApplication<NestFastifyApplication>(new FastifyAdapter());
    await app.init();
    datasheetRecordSubscriptionService = app.get(DatasheetRecordSubscriptionBaseService);
    datasheetRecordSubscriptionRepo = app.get(DatasheetRecordSubscriptionRepository);
    datasheetRecordRepo = app.get(DatasheetRecordRepository);
    datasheetRepo = app.get(DatasheetRepository);
    userRepo = app.get(UserRepository);
  });

  afterAll(async() => {
    await app.close();
  });

  beforeEach(async() => {
    // clear database
    await clearDatabase(getConnection());
    testUser = userRepo.create({ nikeName: 'foo' });
    await userRepo.insert(testUser);

    testDst = datasheetRepo.create({
      spaceId: 'space1', nodeId: 'node1', dstId: 'dst1', dstName: 'Test DST', createdBy: testUser.id
    });
    await datasheetRepo.insert(testDst);

    testRecord1 = datasheetRecordRepo.create({ dstId: testDst.id, recordId: 'rec1' });
    testRecord2 = datasheetRecordRepo.create({ dstId: testDst.id, recordId: 'rec2' });
    testRecord3 = datasheetRecordRepo.create({ dstId: testDst.id, recordId: 'rec3' });
    await datasheetRecordRepo.insert(testRecord1);
    await datasheetRecordRepo.insert(testRecord2);
    await datasheetRecordRepo.insert(testRecord3);
  });

  describe('get subscribed records from datasheet', () => {
    it('should returns empty when no record been subscribed', async() => {
      const result = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(result).toEqual([]);
    });

    it('should returns subscribed record ids from datasheet', async() => {
      const recordSub1 = datasheetRecordSubscriptionRepo.create(
        { dstId: testDst.id, recordId: testRecord1.recordId, createdBy: testUser.id }
      );
      const recordSub2 = datasheetRecordSubscriptionRepo.create(
        { dstId: testDst.id, recordId: testRecord2.recordId, createdBy: testUser.id }
      );
      await datasheetRecordSubscriptionRepo.insert(recordSub1);
      await datasheetRecordSubscriptionRepo.insert(recordSub2);

      const result = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(result.length).toEqual(2);
      expect(result.includes(testRecord1.recordId)).toEqual(true);
      expect(result.includes(testRecord2.recordId)).toEqual(true);
      expect(result.includes(testRecord3.recordId)).toEqual(false);

      await datasheetRecordSubscriptionRepo.delete(recordSub1.id);
      await datasheetRecordSubscriptionRepo.delete(recordSub2.id);
    });
  });

  describe('subscribe record from datasheet', () => {
    it('should not subscribe anything when record id array is empty', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(testUser.id, testDst.id, []);
      const result = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(result.length).toEqual(0);
    });

    it('should subscribe to records', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );
      const result = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(result.length).toEqual(3);
      expect(result.includes(testRecord1.recordId)).toEqual(true);
      expect(result.includes(testRecord2.recordId)).toEqual(true);
      expect(result.includes(testRecord3.recordId)).toEqual(true);
    });

    it('should subscribe to already subscribed records', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );
      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);
      expect(firstTry.includes(testRecord1.recordId)).toEqual(true);
      expect(firstTry.includes(testRecord2.recordId)).toEqual(true);
      expect(firstTry.includes(testRecord3.recordId)).toEqual(true);

      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );

      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(3);
      expect(secondTry.includes(testRecord1.recordId)).toEqual(true);
      expect(secondTry.includes(testRecord2.recordId)).toEqual(true);
      expect(secondTry.includes(testRecord3.recordId)).toEqual(true);
    });
  });

  describe('unsubscribe record from datasheet', () => {
    it('should not unsubscribe anything when record id array is empty', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );

      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);

      await datasheetRecordSubscriptionService.unsubscribeDatasheetRecords(testUser.id, testDst.id, []);
      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(3);
    });

    it('should unsubscribe specific one record', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );

      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);

      await datasheetRecordSubscriptionService.unsubscribeDatasheetRecords(testUser.id, testDst.id, [testRecord1.recordId]);

      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(2);
      expect(secondTry.includes(testRecord1.recordId)).toEqual(false);
      expect(secondTry.includes(testRecord2.recordId)).toEqual(true);
      expect(secondTry.includes(testRecord3.recordId)).toEqual(true);
    });

    it('should unsubscribe multiple records', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );
      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);

      await datasheetRecordSubscriptionService.unsubscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );

      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(0);
    });
  });

  describe('subscribe record from mirror node', () => {
    it('should not subscribe anything when record id array is empty', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(testUser.id, testDst.id, [], 'mirFoo');
      const result = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(result.length).toEqual(0);
    });

    it('should subscribe to records', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId], 'mirFoo'
      );
      const result = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(result.length).toEqual(3);
      expect(result.includes(testRecord1.recordId)).toEqual(true);
      expect(result.includes(testRecord2.recordId)).toEqual(true);
      expect(result.includes(testRecord3.recordId)).toEqual(true);
    });

    it('should subscribe to already subscribed records', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId], 'mirFoo'
      );
      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);
      expect(firstTry.includes(testRecord1.recordId)).toEqual(true);
      expect(firstTry.includes(testRecord2.recordId)).toEqual(true);
      expect(firstTry.includes(testRecord3.recordId)).toEqual(true);

      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId], 'mirFoo'
      );

      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(3);
      expect(secondTry.includes(testRecord1.recordId)).toEqual(true);
      expect(secondTry.includes(testRecord2.recordId)).toEqual(true);
      expect(secondTry.includes(testRecord3.recordId)).toEqual(true);
    });
  });

  describe('unsubscribe record from mirror node', () => {
    it('should not unsubscribe anything when record id array is empty', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId], 'mirFoo'
      );

      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);

      await datasheetRecordSubscriptionService.unsubscribeDatasheetRecords(testUser.id, testDst.id, []);
      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(3);
    });

    it('should unsubscribe specific one record', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId], 'mirFoo'
      );

      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);

      await datasheetRecordSubscriptionService.unsubscribeDatasheetRecords(testUser.id, testDst.id, [testRecord1.recordId]);

      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(2);
      expect(secondTry.includes(testRecord1.recordId)).toEqual(false);
      expect(secondTry.includes(testRecord2.recordId)).toEqual(true);
      expect(secondTry.includes(testRecord3.recordId)).toEqual(true);
    });

    it('should unsubscribe multiple records', async() => {
      await datasheetRecordSubscriptionService.subscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId], 'mirFoo'
      );
      const firstTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(firstTry.length).toEqual(3);

      await datasheetRecordSubscriptionService.unsubscribeDatasheetRecords(
        testUser.id, testDst.id, [testRecord1.recordId, testRecord2.recordId, testRecord3.recordId]
      );

      const secondTry = await datasheetRecordSubscriptionService.getSubscribedRecordIds(testUser.id, testDst.id);
      expect(secondTry.length).toEqual(0);
    });
  });
});
