import React, { useState } from 'react';
import { CUIFormPanel, DataSourceModal, ICUIChatProps, ICUIForm, ISelectProps, registerCUIComponent } from '@apitable/ai';
import { Typography } from '@apitable/components';
import { AddFilled } from '@apitable/icons';
import styles from './style.module.less';

type Props = ISelectProps & ICUIChatProps;

export default function CUIFormSelectDatasheet(props: Props) {
  const { title, description, isComplete } = props;
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const isDisabled = isLoading;

  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  return (
    <CUIFormPanel title={'Select data source'} description={description}>
      <div
        className={styles.selectBox}
        onClick={() => {
          if (!isLoading && !isComplete) {
            setIsModalOpen(true);
          }
        }}
      >
        <AddFilled size={32} />
        <Typography style={{ marginTop: 8 }} variant="body2" align="center">
          You can use your own data source to train the model.
        </Typography>
      </div>
      <DataSourceModal
        open={isModalOpen}
        setOpen={setIsModalOpen}
        onChange={async (data: any) => {
          setIsLoading(true);
          await props.onSubmit(data);
          setIsLoading(false);
        }}
      />
    </CUIFormPanel>
  );
}

registerCUIComponent('CUIFormSelectDatasheet', {
  component: CUIFormSelectDatasheet,
  getDisplayResultWithComponent: (form: ICUIForm, data: any) => {
    console.log(data);
    return data.name;
  },
});
