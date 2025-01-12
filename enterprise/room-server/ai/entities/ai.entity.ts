/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2023 APITable Ltd. <https://apitable.com>
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

import { BaseEntity } from 'shared/entities/base.entity';
import { Column, Entity } from 'typeorm';
import { AIType, AiSetting } from '../models';

@Entity('ai')
export class AiEntity extends BaseEntity {
  @Column({
    name: 'space_id',
    nullable: false,
    length: 50
  })
    spaceId!: string;

  @Column({
    name: 'node_id',
    nullable: false,
    length: 50
  })
    nodeId!: string;

  @Column({
    name: 'ai_id',
    nullable: false,
    unique: true,
    length: 50
  })
    aiId!: string;

  @Column({
    name: 'type',
    nullable: false,
    length: 50
  })
    type!: AIType;

  @Column({
    name: 'name',
    nullable: false,
    length: 255
  })
    name!: string;

  @Column({
    name: 'description',
    nullable: true,
    length: 255
  })
    description?: string;

  @Column({
    name: 'picture',
    nullable: true,
    length: 255
  })
    picture?: string;

  @Column({
    name: 'prologue',
    nullable: true,
    length: 255
  })
    prologue?: string;

  @Column('text', {
    name: 'prompt',
    nullable: true
  })
    prompt?: string;

  @Column({
    name: 'model',
    nullable: true,
    length: 20
  })
    model?: string;

  @Column('json', {
    name: 'setting',
    nullable: false
  })
    setting!: AiSetting;
}
