import { useState } from 'react';
import { Button } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { AddFilled } from '@apitable/icons';
import { ConfigItem } from '../ConfigItem/index';
import { Table } from './components/Table';

export const DataSource = () => {
  const [isTraining, setIsTraining] = useState(false);
  return (
    <div>
      <ConfigItem configTitle={'Datasource'} description={t(Strings.training_data_source_title)}>
        <Button color={'primary'} prefixIcon={<AddFilled />}>
          {t(Strings.training_add_data_source_btn_text)}
        </Button>
      </ConfigItem>
      <Table setIsTraining={setIsTraining} />
    </div>
  );
};
