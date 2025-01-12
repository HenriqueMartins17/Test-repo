import { DeleteOutlined, RefreshOutlined } from '@apitable/icons';
import { LinkButton, useThemeColors } from '@apitable/components';
import classnames from 'classnames';

interface IButtonAreaProps {
  onTraining?: () => void;
  onDelete?: () => void;
  disabled?: boolean;
}

export const ButtonArea: React.FC<IButtonAreaProps> = ({ onDelete, onTraining, disabled }) => {
  const colors = useThemeColors();
  return (
    <div className={'vk-flex vk-items-center vk-space-x-4'}>
      <LinkButton
        prefixIcon={<RefreshOutlined color={colors.primaryColor} size={12} />}
        color={colors.primaryColor}
        className={classnames({
          'vk-invisible': !Boolean(onTraining),
        })}
        underline={false}
        disabled={disabled}
      >
        Train
      </LinkButton>
      {Boolean(onDelete) && (
        <LinkButton
          prefixIcon={<DeleteOutlined color={colors.textDangerDefault} size={12} />}
          color={colors.textDangerDefault}
          onClick={onDelete}
          disabled={disabled}
          underline={false}
        >
          Train
        </LinkButton>
      )}
    </div>
  );
};
