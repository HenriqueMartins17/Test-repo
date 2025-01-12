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

@Entity('ai_conversation')
export class AiConversationEntity extends BaseEntity {
    @Column({
      name: 'ai_id',
      nullable: false,
      length: 50,
    })
      aiId!: string;

    @Column({
      name: 'training_id',
      nullable: true,
      length: 50,
    })
      trainingId?: string;

    @Column({
      name: 'conversation_id',
      nullable: false,
      length: 50,
    })
      conversationId!: string;

    @Column({
      name: 'title',
      nullable: false,
      length: 255,
    })
      title!: string;

    @Column({
      name: 'origin',
      nullable: false,
      length: 20,
    })
      origin!: string;

    @Column({
      name: 'origin_type',
      nullable: false,
      length: 20,
    })
      originType!: string;
}
