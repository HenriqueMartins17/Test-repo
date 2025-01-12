import { Training } from '@apitable/ai';

interface ITrainProps {
  visible: boolean;
  close: () => void;
}

export const Train: React.FC<ITrainProps> = (props) => {
  return (
    <Training
      {...props}
      tabConfig={{
        Local: null,
      }}
    />
  );
};
