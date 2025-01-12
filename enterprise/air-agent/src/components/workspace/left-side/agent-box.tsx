import { Menu, Dropdown } from 'antd';
import { updateAiAgent } from 'api/workspace';
import { useRouter } from 'next/router';
import { useState, useEffect, useRef } from 'react';
import { TextInput, colors } from '@apitable/components';
import { DeleteOutlined, EditOutlined, MoreStandOutlined, RobotOutlined } from '@apitable/icons';
import { Popconfirm } from '../../popconfirm/index';
import styles from './agent-box.module.scss';

interface IAgentBoxProps {
  agent: IAiAgentInfoProps;
  onDeleteAgent: (agentId: string) => void;
  aiId: string | undefined;
  refreshUserAgentInfo: () => Promise<void>;
}

const AgentBox = (props: IAgentBoxProps) => {
  const router = useRouter();

  const [agentName, setAgentName] = useState(props.agent.agentName);
  const [error, setError] = useState<boolean>(false);
  const [renameEnabled, setRenameEnabled] = useState<boolean>(false);

  const dropdownLink = useRef<HTMLAnchorElement | null>(null);

  const [isPopoverOpen, setIsPopoverOpen] = useState<boolean>(false);

  const [isHovering, setIsHovering] = useState<boolean>(false);

  useEffect(() => {
    // setIsNewAgent(props.agent.airagentName === 'New Agent');
  }, [props.agent.agentName]);

  const handleRightClick = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    e.preventDefault();
    // programmatically click the dropdown link to show the dropdown
    dropdownLink.current?.click();
  };

  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setAgentName(e.target.value);
  };

  const updateAgentInfo = async () => {
    try {
      await updateAiAgent(props.agent.aiId, {
        name: agentName,
      });
      await props.refreshUserAgentInfo();
      setRenameEnabled(false);
    } catch (error) {
      console.error('Error updating agent:', error);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      updateAgentInfo();
    }
  };

  const MenuContent = () => {
    return (
      <Menu className={styles.menu}>
        <Menu.Item
          key="renameAgent"
          onClick={() => {
            setRenameEnabled(true);
          }}
        >
          <div className={styles.menuItem}>
            <EditOutlined color={colors.textCommonPrimary} />
            <div>Rename</div>
          </div>
        </Menu.Item>
        <Menu.Item key="deleteAgent">
          <div
            className={styles.menuItem}
            onClick={() => {
              setIsPopoverOpen(true);
            }}
          >
            <DeleteOutlined color={colors.textCommonPrimary} />
            <div>Delete</div>
          </div>
        </Menu.Item>
      </Menu>
    );
  };

  return (
    <Popconfirm
      title="Confirm to delete?"
      okText="Yes"
      cancelText="No"
      onOk={() => {
        props.onDeleteAgent(props.agent.agentId);
        location.href = '/workspace';
      }}
      onCancel={() => {
        setIsPopoverOpen(false);
      }}
      open={isPopoverOpen}
      type="danger"
    >
      <div
        id={props.agent.agentId}
        className={props.aiId == props.agent.agentId ? styles.agentInfoItemSelected : styles.agentInfoItem}
        onClick={() => {
          router.push(`/workspace/${props.agent.agentId}`);
        }}
        onContextMenu={handleRightClick} // Add the right-click handler here
        onMouseEnter={() => setIsHovering(true)}
        onMouseLeave={() => setIsHovering(false)}
      >
        <div className={styles.agentInfoItemTitle}>
          <div
            style={{
              height: '16px',
              width: '16px',
            }}
          >
            <RobotOutlined
              size={16}
              color={props.aiId == props.agent.agentId ? colors.textBrandDefault : colors.textCommonPrimary}
              className={styles.agentInfoItemTitleIcon}
            />
          </div>
          {renameEnabled ? (
            <TextInput
              height={100}
              className={styles.renameInput}
              value={agentName}
              onChange={handleNameChange}
              onBlur={updateAgentInfo}
              onKeyPress={handleKeyPress}
              autoFocus
            />
          ) : (
            <div>{agentName}</div>
          )}
        </div>
        {isHovering && !renameEnabled && (
          <Dropdown overlay={MenuContent()} trigger={['click']} className={styles.dropdownSelect}>
            <a
              ref={dropdownLink}
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
              }}
            >
              <MoreStandOutlined color={colors.textCommonPrimary} />
            </a>
          </Dropdown>
        )}
        {/* {renameEnabled && (
          <div onClick={updateAgentInfo} className={styles.renameTick}>
            <CheckOutlined color={colors.textCommonPrimary} />
          </div>
        )} */}
      </div>
    </Popconfirm>
  );
};

export default AgentBox;
