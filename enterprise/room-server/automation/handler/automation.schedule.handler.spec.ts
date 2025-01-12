/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify';
import { Test, TestingModule } from '@nestjs/testing';
import { AppModule } from 'app.module';
import { AutomationTriggerEntity } from 'automation/entities/automation.trigger.entity';
import { TriggerEventHelper } from 'automation/events/helpers/trigger.event.helper';
import { AutomationRobotRepository } from 'automation/repositories/automation.robot.repository';
import { AutomationTriggerRepository } from 'automation/repositories/automation.trigger.repository';
import { AutomationService } from 'automation/services/automation.service';
import { AutomationTriggerScheduleEntity, TriggerStatus } from 'enterprise/automation/entities/automation.trigger.schedule.entity';
import { AutomationScheduleHandler } from 'enterprise/automation/handler/automation.schedule.handler';
import { AutomationTriggerScheduleRepository } from 'enterprise/automation/repositories/automation.trigger.schedule.repository';
import { QueueSenderService } from 'shared/services/queue/queue.sender.service';

describe('AutomationScheduleHandler', () => {
  let app: NestFastifyApplication;
  let handler: AutomationScheduleHandler;

  const mockGetSchedule = jest.spyOn(AutomationTriggerScheduleRepository.prototype, 'selectBaseInfoById');
  const mockUpdateSchedule = jest.spyOn(AutomationTriggerScheduleRepository.prototype, 'update');
  const mockGetTrigger = jest.spyOn(AutomationTriggerRepository.prototype, 'selectTriggerByTriggerId');
  const mockGetRobot = jest.spyOn(AutomationRobotRepository.prototype, 'selectRobotBaseInfoDtoByRobotIds');
  const mockMqSendMessage = jest.spyOn(QueueSenderService.prototype, 'sendMessage');
  const mockGetSpaceIdByRobotId = jest.spyOn(AutomationService.prototype, 'getSpaceIdByRobotId');
  const mockIsAutomationRunnableInSpace = jest.spyOn(AutomationService.prototype, 'isAutomationRunnableInSpace');
  const mockCreateRunHistory = jest.spyOn(AutomationService.prototype, 'createRunHistory');
  const mockUpdateTaskRunHistory = jest.spyOn(AutomationService.prototype, 'updateTaskRunHistory');
  const mockGetRobotByRobotIdAndTriggerId = jest.spyOn(AutomationService.prototype, 'getRobotByRobotIdAndTriggerId');
  const mockHandleTask = jest.spyOn(AutomationService.prototype, 'handleTask');
  const mockRenderInput = jest.spyOn(TriggerEventHelper.prototype, 'renderInput');

  beforeAll(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();
    app = module.createNestApplication<NestFastifyApplication>(new FastifyAdapter());
    await app.init();
    handler = app.get(AutomationScheduleHandler);
  });

  afterAll(async () => {
    await app.close();
  });

  describe('getCronExpressionFromScheduleConfig', () => {
    jest.setTimeout(60000);
    it('second--out of range', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          dayOfMonth: '*',
          dayOfWeek: '*',
          hour: '*',
          minute: '*',
          month: '*',
          timeZone: 'UTC',
          second: '70',
        });
      }).toThrow(new Error('Constraint error, got value 70 expected range 0-59'));
    });

    it('minute--out of range', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          dayOfMonth: '*',
          dayOfWeek: '*',
          hour: '*',
          month: '*',
          timeZone: 'UTC',
          second: '0',
          minute: '70',
        });
      }).toThrow(new Error('Constraint error, got value 70 expected range 0-59'));
    });

    it('hour--out of range', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          dayOfMonth: '*',
          dayOfWeek: '*',
          month: '*',
          timeZone: 'UTC',
          second: '0',
          minute: '30',
          hour: '25',
        });
      }).toThrow(new Error('Constraint error, got value 25 expected range 0-23'));
    });

    it('dayOfMonth--out of range 33', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          dayOfWeek: '*',
          month: '*',
          timeZone: 'UTC',
          second: '0',
          minute: '30',
          hour: '1',
          dayOfMonth: '33,L',
        });
      }).toThrow(new Error('Constraint error, got value 33 expected range 1-31'));
    });

    it('dayOfMonth--out of range M', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          dayOfWeek: '*',
          month: '*',
          timeZone: 'UTC',
          second: '0',
          minute: '30',
          hour: '1',
          dayOfMonth: '31,M',
        });
      }).toThrow(new Error('Invalid characters, got value: 31,M'));
    });

    it('month--out of range 14', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          dayOfWeek: '*',
          timeZone: 'UTC',
          second: '0',
          minute: '30',
          hour: '1',
          month: '14',
          dayOfMonth: 'L',
        });
      }).toThrow(new Error('Constraint error, got value 14 expected range 1-12'));
    });

    it('month--out of range 0', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          dayOfWeek: '*',
          timeZone: 'UTC',
          second: '0',
          minute: '30',
          hour: '1',
          month: '0',
          dayOfMonth: 'L',
        });
      }).toThrow(new Error('Constraint error, got value 0 expected range 1-12'));
    });

    it('dayOfWeek--out of range 8', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          timeZone: 'UTC',
          second: '0',
          minute: '30',
          hour: '1',
          month: '1',
          dayOfMonth: 'L',
          dayOfWeek: '8',
        });
      }).toThrow(new Error('Constraint error, got value 8 expected range 0-7'));
    });

    it('dayOfMonth-- support for L', () => {
      expect(() => {
        handler.getCronExpressionFromScheduleConfig({
          timeZone: 'UTC',
          second: '0',
          minute: '30',
          hour: '1',
          dayOfMonth: 'L',
          month: '1',
          dayOfWeek: '0',
        });
      }).not.toThrow();
    });

    it('every week on sun--should be [0 0 0 * * 0]', () => {
      const expression = handler.getCronExpressionFromScheduleConfig({
        timeZone: 'UTC',
        second: '0',
        minute: '0',
        hour: '0',
        month: '*',
        dayOfMonth: '*',
        dayOfWeek: '0',
      } as any);
      const cronString = expression.stringify(true);
      expect(cronString).toBe('0 0 0 * * 0');
    });

    it('every week on 1, 2, 3 4, 5, 6--should be [0 0 0 * * *]', () => {
      const expression = handler.getCronExpressionFromScheduleConfig({
        timeZone: 'America/Noronha',
        minute: '0',
        hour: '0',
        month: '*',
        dayOfMonth: '*',
        dayOfWeek: '0,1,2,3,4,5,6',
      } as any);
      const cronString = expression.stringify(true);
      expect(cronString).toBe('0 0 0 * * *');
    });

    it('every last day of month--should be [0 0 0 L * *]', () => {
      const expression = handler.getCronExpressionFromScheduleConfig({
        timeZone: 'UTC',
        second: '0',
        minute: '0',
        hour: '0',
        month: '*',
        dayOfMonth: 'L',
        dayOfWeek: '*',
      } as any);
      const cronString = expression.stringify(true);
      expect(cronString).toBe('0 0 0 L * *');
    });

    it('every 5 minutes--should be [0 */5 * * * *]', () => {
      const expression = handler.getCronExpressionFromScheduleConfig({
        timeZone: 'UTC',
        second: '0',
        minute: '*/5',
        hour: '*',
        month: '*',
        dayOfMonth: '*',
        dayOfWeek: '*',
      } as any);
      const cronString = expression.stringify(true);
      expect(cronString).toBe('0 */5 * * * *');
    });

    it('every 10 minutes--should be [0 */10 * * * *]', () => {
      const expression = handler.getCronExpressionFromScheduleConfig({
        timeZone: 'UTC',
        minute: '*/10',
        hour: '*',
        month: '*',
        dayOfMonth: '*',
        dayOfWeek: '*',
      } as any);
      const cronString = expression.stringify(true);
      expect(cronString).toBe('0 */10 * * * *');
    });

    it('every month at L,1--should be [0 0 * 1,L * *]', () => {
      const expression = handler.getCronExpressionFromScheduleConfig({
        timeZone: 'UTC',
        second: '0',
        minute: '0',
        hour: '*',
        month: '*',
        dayOfMonth: '1,L',
        dayOfWeek: '*',
      } as any);
      const cronString = expression.stringify(true);
      expect(cronString).toBe('0 0 * 1,L * *');
    });
  });

  describe('getScheduleTrigger', () => {
    jest.setTimeout(60000);
    beforeEach(() => {
      mockGetSchedule.mockClear();
      mockGetTrigger.mockClear();
      mockGetRobot.mockClear();
    });

    it('schedule delete--should return undefined', async () => {
      mockGetSchedule.mockResolvedValue(undefined);
      const { schedule, trigger } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).toBe(undefined);
      expect(trigger).toBe(undefined);
    });

    it('schedule config is empty object--should return undefined', async () => {
      const entity: AutomationTriggerScheduleEntity = {
        createdAt: new Date(),
        createdBy: '',
        isDeleted: false,
        spaceId: '',
        updatedBy: '',
        beforeInsert(): void {},
        id: 'test_id',
        scheduleConf: {} as any,
        triggerStatus: TriggerStatus.PENDING,
        isPushed: true,
        triggerId: 'test_trigger_id',
        triggerNextTime: new Date(),
      };
      mockGetSchedule.mockResolvedValue(entity);
      const { schedule, trigger } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).toBe(undefined);
      expect(trigger).toBe(undefined);
    });

    it('schedule is Running--should return undefined', async () => {
      const entity: AutomationTriggerScheduleEntity = {
        createdAt: new Date(),
        createdBy: '',
        isDeleted: false,
        spaceId: '',
        updatedBy: '',
        beforeInsert(): void {},
        id: 'test_id',
        scheduleConf: {
          timeZone: 'test',
        } as any,
        triggerStatus: TriggerStatus.RUNNING,
        isPushed: true,
        triggerId: 'test_trigger_id',
        triggerNextTime: new Date(),
      };
      mockGetSchedule.mockResolvedValue(entity);
      mockMqSendMessage.mockImplementation();
      const { schedule, trigger } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).toBe(undefined);
      expect(trigger).toBe(undefined);
    });

    it('schedule is Running more than 5 minutes--should return normal', async () => {
      const scheduleEntity: AutomationTriggerScheduleEntity = {
        createdAt: new Date(),
        updatedAt: new Date(new Date().getTime() - 5 * 60 * 1000).toString() as any,
        createdBy: '',
        isDeleted: false,
        spaceId: '',
        updatedBy: '',
        beforeInsert(): void {},
        id: 'test_id',
        scheduleConf: {
          timeZone: 'test',
        } as any,
        triggerStatus: TriggerStatus.RUNNING,
        isPushed: true,
        triggerId: 'test_trigger_id',
        triggerNextTime: new Date(),
      };
      const triggerEntity: AutomationTriggerEntity = {
        createdAt: new Date(),
        createdBy: '',
        id: 'test_id_d',
        isDeleted: false,
        robotId: 'test_robot_id',
        triggerId: 'test_trigger_id',
        updatedBy: '',
        beforeInsert(): void {},
      };
      mockGetSchedule.mockResolvedValueOnce(scheduleEntity);
      mockGetTrigger.mockResolvedValue(triggerEntity);
      mockMqSendMessage.mockImplementation();
      const { schedule, trigger, shouldReQueue } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).not.toBe(undefined);
      expect(trigger).not.toBe(undefined);
      expect(shouldReQueue).toBe(true);
    });

    it('trigger is delete--should return undefined', async () => {
      const entity: AutomationTriggerScheduleEntity = {
        createdAt: new Date(),
        createdBy: '',
        isDeleted: false,
        spaceId: '',
        updatedBy: '',
        beforeInsert(): void {},
        id: 'test_id',
        scheduleConf: {
          timeZone: 'test',
        } as any,
        triggerStatus: TriggerStatus.PENDING,
        isPushed: true,
        triggerId: 'test_trigger_id',
        triggerNextTime: new Date(),
      };
      mockGetSchedule.mockResolvedValue(entity);
      mockGetTrigger.mockResolvedValue(undefined);
      const { schedule, trigger, shouldReQueue } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).toBe(undefined);
      expect(trigger).toBe(undefined);
      expect(shouldReQueue).toBe(false);
    });

    it('robot deleted--should return undefined', async () => {
      const scheduleEntity: AutomationTriggerScheduleEntity = {
        createdAt: new Date(),
        createdBy: '',
        isDeleted: false,
        spaceId: '',
        updatedBy: '',
        beforeInsert(): void {},
        id: 'test_id',
        scheduleConf: {
          timeZone: 'test',
        } as any,
        triggerStatus: TriggerStatus.PENDING,
        isPushed: true,
        triggerId: 'test_trigger_id',
        triggerNextTime: new Date(),
      };
      const entity: AutomationTriggerEntity = {
        createdAt: new Date(),
        createdBy: '',
        id: 'test_id_d',
        isDeleted: false,
        robotId: 'test_robot_id',
        triggerId: 'test_trigger_id',
        updatedBy: '',
        beforeInsert(): void {},
      };
      mockGetSchedule.mockResolvedValueOnce(scheduleEntity);
      mockGetTrigger.mockResolvedValue(entity);
      mockGetRobot.mockResolvedValue([]);
      const { schedule, trigger, shouldReQueue } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).toBe(undefined);
      expect(trigger).toBe(undefined);
      expect(shouldReQueue).toBe(false);
    });

    it('robot un active--should return undefined', async () => {
      const scheduleEntity: AutomationTriggerScheduleEntity = {
        createdAt: new Date(),
        createdBy: '',
        isDeleted: false,
        spaceId: '',
        updatedBy: '',
        beforeInsert(): void {},
        id: 'test_id',
        scheduleConf: {
          timeZone: 'test',
        } as any,
        triggerStatus: TriggerStatus.PENDING,
        isPushed: true,
        triggerId: 'test_trigger_id',
        triggerNextTime: new Date(),
      };
      const entity: AutomationTriggerEntity = {
        createdAt: new Date(),
        createdBy: '',
        id: 'test_id_d',
        isDeleted: false,
        robotId: 'test_robot_id',
        triggerId: 'test_trigger_id',
        updatedBy: '',
        beforeInsert(): void {},
      };
      mockGetSchedule.mockResolvedValueOnce(scheduleEntity);
      mockGetTrigger.mockResolvedValue(entity);
      mockGetRobot.mockResolvedValue([
        {
          name: 'test_robot',
          description: '',
          isActive: false,
          robotId: 'test__robot_id',
        },
      ]);
      const { schedule, trigger, shouldReQueue } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).toBe(undefined);
      expect(trigger).toBe(undefined);
      expect(shouldReQueue).toBe(true);
    });

    it('robot active--should return normal', async () => {
      const scheduleEntity: AutomationTriggerScheduleEntity = {
        createdAt: new Date(),
        createdBy: '',
        isDeleted: false,
        spaceId: '',
        updatedBy: '',
        beforeInsert(): void {},
        id: 'test_id',
        scheduleConf: {
          timeZone: 'test',
        } as any,
        triggerStatus: TriggerStatus.PENDING,
        isPushed: true,
        triggerId: 'test_trigger_id',
        triggerNextTime: new Date(),
      };
      const entity: AutomationTriggerEntity = {
        createdAt: new Date(),
        createdBy: '',
        id: 'test_id_d',
        isDeleted: false,
        robotId: 'test_robot_id',
        triggerId: 'test_trigger_id',
        updatedBy: '',
        beforeInsert(): void {},
      };
      mockGetSchedule.mockResolvedValueOnce(scheduleEntity);
      mockGetTrigger.mockResolvedValue(entity);
      mockGetRobot.mockResolvedValue([
        {
          name: 'test_robot',
          description: '',
          isActive: true,
          robotId: 'test__robot_id',
        },
      ]);
      const { schedule, trigger } = await handler.getScheduleTrigger({
        scheduleId: 'test',
      });
      expect(schedule).not.toBe(undefined);
      expect(trigger).not.toBe(undefined);
    });
  });

  describe('handleScheduleTask', () => {
    jest.setTimeout(60000);
    beforeEach(() => {
      mockGetSchedule.mockClear();
      mockGetTrigger.mockClear();
      mockGetRobot.mockClear();
      mockUpdateSchedule.mockClear();
      mockGetSpaceIdByRobotId.mockClear();
      mockIsAutomationRunnableInSpace.mockClear();
      mockCreateRunHistory.mockClear();
      mockUpdateTaskRunHistory.mockClear();
      mockGetRobotByRobotIdAndTriggerId.mockClear();
      mockGetRobotByRobotIdAndTriggerId.mockClear();
      mockHandleTask.mockClear();
      mockRenderInput.mockClear();
    });

    it('schedule undefined--should return Nack', async () => {
      mockGetSchedule.mockResolvedValue(undefined);
      const result = await handler.handleScheduleTask({
        scheduleId: 'test',
      });
      expect(mockUpdateSchedule).not.toBeCalled();
      expect(result).toStrictEqual(undefined);
    });

    it('trigger undefined--should return Nack', async () => {
      mockGetSchedule.mockResolvedValue({} as any);
      mockGetTrigger.mockResolvedValue(undefined);
      const result = await handler.handleScheduleTask({
        scheduleId: 'test',
      });
      expect(mockUpdateSchedule).not.toBeCalled();
      expect(result).toStrictEqual(undefined);
    });

    it('trigger and schedule not undefined--should handle schedule', async () => {
      mockGetSchedule.mockResolvedValue({
        id: '111',
        scheduleConf: { second: '0', hour: '*', month: '*', minute: '*', timeZone: 'Asia/Hong_Kong', dayOfWeek: '*', dayOfMonth: '*' },
        triggerStatus: 0,
        triggerId: 'test_trigger_id',
        triggerNextTime: undefined,
      } as any);
      mockGetTrigger.mockResolvedValue({} as any);
      mockGetRobot.mockResolvedValue([
        {
          name: 'test_robot',
          description: '',
          isActive: true,
          robotId: 'test_robot_id',
        },
      ]);
      mockMqSendMessage.mockImplementation();
      mockUpdateSchedule.mockImplementation();
      mockHandleTask.mockImplementation();
      mockUpdateSchedule.mockImplementation();
      mockRenderInput.mockImplementation();
      await handler.handleScheduleTask({
        scheduleId: 'test',
      });
      expect(mockHandleTask).toBeCalled();
    });

    it('handle trigger--every 1 minute', async () => {
      mockGetSchedule.mockResolvedValue({
        id: '111',
        scheduleConf: { second: '0', hour: '*', month: '*', minute: '*', timeZone: 'Asia/Hong_Kong', dayOfWeek: '*', dayOfMonth: '*' },
        triggerStatus: 0,
        triggerId: 'test_trigger_id',
        triggerNextTime: undefined,
      } as any);
      mockGetTrigger.mockResolvedValue({
        robotId: 'test_robot_id',
        triggerId: 'test_trigger_id',
        triggerTypeId: 'test_trigger_type_id',
        input: {
          type: 'Expression',
          value: {
            operands: [
              'timeZone',
              { type: 'Literal', value: 'Asia/Hong_Kong' },
              'scheduleType',
              { type: 'Literal', value: 'hour' },
              'scheduleRule',
              {
                type: 'Expression',
                value: {
                  operands: [
                    'dayOfWeek',
                    { type: 'Literal', value: '*' },
                    'minute',
                    { type: 'Literal', value: '8' },
                    'month',
                    { type: 'Literal', value: '*' },
                    'hour',
                    { type: 'Literal', value: '*' },
                    'dayOfMonth',
                    { type: 'Literal', value: '*' },
                  ],
                  operator: 'newObject',
                },
              },
            ],
            operator: 'newObject',
          },
        },
        resourceId: '',
      } as any);
      mockGetRobot.mockResolvedValue([
        {
          name: 'test_robot',
          description: '',
          isActive: true,
          robotId: 'test__robot_id',
        },
      ]);
      mockMqSendMessage.mockImplementation();
      mockUpdateSchedule.mockImplementation();
      mockGetSpaceIdByRobotId.mockResolvedValue('test_space_id');
      mockIsAutomationRunnableInSpace.mockResolvedValue(true);
      mockCreateRunHistory.mockImplementation();
      mockUpdateTaskRunHistory.mockImplementation();
      mockGetRobotByRobotIdAndTriggerId.mockResolvedValue({
        id: 'test_robot_id',
        triggerId: 'test_trigger_id',
        triggerTypeId: 'test_trigger_type_id',
        entryActionId: '',
        // action map;
        actionsById: {
          test_action_id: {
            action_type_id: 'test_action_type_id',
          } as any,
        },
        // actionType map;
        actionTypesById: {
          test_action_type_id: {} as any,
        },
      });
      await handler.handleScheduleTask({
        scheduleId: 'test',
      });
      expect(mockHandleTask).toBeCalled();
    });

    it('schedule should be executed--at America/Noronha[0 * * * * * ] ', async () => {
      mockGetSchedule.mockResolvedValue({
        id: '111',
        scheduleConf: { second: '0', hour: '*', month: '*', minute: '*', timeZone: 'America/Noronha', dayOfWeek: '*', dayOfMonth: '*' },
        triggerStatus: 0,
        triggerId: 'test_trigger_id',
        triggerNextTime: undefined,
      } as any);
      mockGetTrigger.mockResolvedValue({
        robotId: 'test_robot_id',
        triggerId: 'test_trigger_id',
        triggerTypeId: 'test_trigger_type_id',
        input: {},
        resourceId: '',
      } as any);
      mockGetRobot.mockResolvedValue([
        {
          name: 'test_robot',
          description: '',
          isActive: true,
          robotId: 'test__robot_id',
        },
      ]);
      mockMqSendMessage.mockImplementation();
      mockUpdateSchedule.mockImplementation();
      mockHandleTask.mockImplementation();
      mockUpdateSchedule.mockImplementation();
      mockRenderInput.mockImplementation();
      await handler.handleScheduleTask({
        scheduleId: 'test',
      });
      expect(mockHandleTask).toBeCalled();
    });
  });
});
