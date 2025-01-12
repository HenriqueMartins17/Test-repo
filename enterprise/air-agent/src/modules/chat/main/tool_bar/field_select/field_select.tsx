import { Col, Row } from 'antd';
import { useState } from 'react';
import { usePopper } from 'react-popper';
import { FieldType } from '@apitable/core';
import { ArrowRightOutlined } from '@apitable/icons';
// import { LineSearchInput } from 'pc/components/list/common_list/line_search_input';
// import { getFieldTypeIcon } from 'pc/components/multi_grid/field_setting';
import { SelectItem } from './select_item';
import styles from './style.module.less';

export const FieldSelect = () => {
  const [showList, setShowList] = useState(false);
  const [referenceElement, setReferenceElement] = useState<HTMLElement | null>(null);
  const [popperElement, setPopperElement] = useState<HTMLElement | null>(null);
  const { styles: popperStyle, attributes } = usePopper(referenceElement, popperElement, {
    modifiers: [
      {
        name: 'offset',
        options: {
          offset: [0, 4],
        },
      },
      {
        name: 'flip',
        options: {
          fallbackPlacements: ['top', 'right'],
        },
      },
    ],
  });

  const options = [
    {
      label: 'label',
      value: 1,
      // prefixIcon: getFieldTypeIcon(FieldType.SingleText),
    },
  ];

  const clickTrigger = () => {
    setShowList((pre) => !pre);
  };

  return (
    <>
      <Row className={styles.triggerContainer} align={'middle'} onClick={clickTrigger} ref={setReferenceElement}>
        <Col flex={1}>trigger</Col>
        <Col className={styles.arrow}>
          <ArrowRightOutlined />
        </Col>
      </Row>
      {showList && (
        <div
          ref={(node) => {
            setPopperElement(node);
          }}
          style={popperStyle.popper}
          {...attributes.popper}
          className={styles.dropdown}
        >
          <SelectItem label={'选择全部'} checked />
          {/* <LineSearchInput className={styles.searchInput} /> */}
          {/* {
          options.map(option => {
            return <SelectItem label={option.label} key={option.value} checked prefixIcon={option.prefixIcon}/>;
          })
        } */}
        </div>
      )}
    </>
  );
};
