import * as React from 'react';
import { useEffect, useState } from 'react';
import { useDispatch } from 'react-redux';
import { AvatarGroup } from '@apitable/components';
import {
  Api,
  IDPrefix,
  integrateCdnHost,
  IUnitValue,
  MemberType,
  OtherTypeUnitId,
  Selectors,
  Settings,
  StoreActions,
  Strings,
  t,
} from '@apitable/core';
// eslint-disable-next-line no-restricted-imports
import { Avatar, AvatarSize, Tooltip } from 'pc/components/common';
import { useAppSelector } from 'pc/store/react-redux';
import { getSocialWecomUnitName } from '../../../home';
import { IWorkdocCollaborators } from '../interface';
import styles from './style.module.less';

const MAX_SHOW_NUMBER = 5;

export const WorkdocCollaborators = (props: IWorkdocCollaborators) => {
  const { collaborators } = props;

  const dispatch = useDispatch();
  const unitMap = useAppSelector((state) => Selectors.getUnitMap(state));
  const spaceInfo = useAppSelector((state) => state.space.curSpaceInfo);
  const [collaboratorsUnits, setCollaboratorsUnits] = useState<IUnitValue[]>([]);

  useEffect(() => {
    const units: IUnitValue[] = [];
    collaborators.forEach((unitId) => {
      const isAnonymous = unitId.startsWith(IDPrefix.WorkDocAonymousId);
      if (isAnonymous) {
        units.push({
          type: MemberType.Member,
          userId: OtherTypeUnitId.Alien,
          unitId: OtherTypeUnitId.Alien,
          avatar: integrateCdnHost(Settings.datasheet_unlogin_user_avatar.value),
          name: `${t(Strings.alien)} ${unitId.replace(IDPrefix.WorkDocAonymousId, '')}`,
          isActive: true,
        });
      } else {
        const unit = unitMap?.[unitId];
        if (unit) {
          units.push(unit);
        } else {
          Api.loadOrSearch({ unitIds: unitId }).then((res) => {
            const {
              data: { data: resData, success },
            } = res;
            if (!resData?.length || !success) {
              return;
            }
            const newUser = resData[0];
            dispatch(
              StoreActions.updateUnitMap({
                [unitId]: newUser,
              })
            );
            units.push(newUser);
          });
        }
      }
    });

    setCollaboratorsUnits(units);
  }, [collaborators, dispatch, unitMap]);

  return (
    <div className={styles.workdocCollaborators}>
      <AvatarGroup
        max={MAX_SHOW_NUMBER}
        size="xs"
        maxStyle={{ cursor: 'pointer' }}
        popoverContent={
          <div className={styles.moreCollaborators}>
            {collaboratorsUnits.slice(MAX_SHOW_NUMBER).map((unit) => {
              const title =
                getSocialWecomUnitName?.({
                  name: unit?.name,
                  isModified: unit?.isMemberNameModified,
                  spaceInfo,
                }) || unit?.name;
              return (
                <div key={unit.unitId} className={styles.moreCollaboratorItem}>
                  <Avatar
                    id={unit.unitId}
                    title={unit.nickName || unit.name}
                    avatarColor={unit.avatarColor}
                    src={unit.avatar}
                    size={AvatarSize.Size24}
                  />
                  <span className={styles.moreCollaboratorName}>{title}</span>
                </div>
              );
            })}
          </div>
        }
      >
        {collaboratorsUnits.map((unit) => {
          const title =
            getSocialWecomUnitName?.({
              name: unit?.name,
              isModified: unit?.isMemberNameModified,
              spaceInfo,
            }) || unit?.name;
          return (
            <Tooltip key={unit.unitId} title={title}>
              <span>
                <Avatar
                  id={unit.unitId}
                  title={unit.nickName || unit.name}
                  avatarColor={unit.avatarColor}
                  src={unit.avatar}
                  size={AvatarSize.Size24}
                />
              </span>
            </Tooltip>
          );
        })}
      </AvatarGroup>
    </div>
  );
};
