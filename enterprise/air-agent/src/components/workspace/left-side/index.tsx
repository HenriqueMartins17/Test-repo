import { Button, message, Progress } from 'antd';
import { IUserProfile } from 'api/user';
import { createAiAgent, deleteAiAgent } from 'api/workspace';
import { showStripePricingTable } from 'components/billing/show_stripe_pricing_table';
import AgentBox from 'components/workspace/left-side/agent-box';
import UserDropdown from 'components/workspace/left-side/user-dropdown';
import useTheme from 'context/global/theme';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/router';
import { colors } from '@apitable/components';
import { AddOutlined, VideoFilled } from '@apitable/icons';
import { ThemeName } from '../../../../utils/theme';
import styles from './index.module.scss';

interface IWorkspaceLeftSideProps {
  className?: string;
  userAgentInfo: IAiAgentInfoProps[];
  refreshUserAgentInfo: () => Promise<void>;
  aiId: string | undefined;
  user: IUserProfile;
  theme?: ThemeName;
  setTheme: (theme: ThemeName) => void;
}

const WorkspaceLeftSide = (props: IWorkspaceLeftSideProps) => {
  const router = useRouter();

  const currentTheme = useTheme();

  const createAgent = async () => {
    try {
      const response = await createAiAgent({
        preAgentId: undefined,
        name: 'New Agent',
      });
      return response;
    } catch (error) {
      message.error('Error when creating agent' + error);
      console.error('Error creating agent:', error);
      return null;
    }
  };

  const showPricingTableModal = () => {
    showStripePricingTable(props.user.email);
  };

  const handleOnCreateAgent = async () => {
    try {
      const response: any = await createAgent();

      const agentId = response?.data.ai.id;

      await props.refreshUserAgentInfo();

      await router.push('/workspace/' + agentId);
    } catch (error) {
      console.error('Error during agent creation:', error);
    }
  };

  const onDeleteAgent = async (agentId: string) => {
    try {
      const response = await deleteAiAgent(agentId);
      message.success('Agent deleted successfully');
      console.log('Delete response:', response);
    } catch (error) {
      message.error('Error when deleting agent' + error);
      console.error('Error deleting agent:', error);
    }
  };

  return (
    <div className={props.className + ' ' + styles.leftSideContent}>
      <div className={styles.leftSideHeader}>
        <div className={styles.headerLogo}>
          <Link href="/" passHref>
            <Image src={currentTheme.theme == 'light' ? '/file/img/logo.black.svg' : '/file/img/logo.white.svg'} alt="Logo" width={200} height={36} />
          </Link>
        </div>
        <div className={styles.headerAgent}>
          <Button onClick={handleOnCreateAgent} className={styles.createButton}>
            <AddOutlined size={16} />
            <div>New Agent</div>
          </Button>
          <div className={styles.agentList}>
            {props.userAgentInfo?.map((agent: any) => {
              return (
                <AgentBox
                  agent={agent}
                  key={agent.aiId}
                  onDeleteAgent={onDeleteAgent}
                  aiId={props.aiId}
                  refreshUserAgentInfo={props.refreshUserAgentInfo}
                />
              );
            })}
          </div>
        </div>
      </div>
      <div className={styles.leftSideFooter}>
        <div className={styles.footerTop}>
          <div className={styles.footerVideo}>
            <div className={styles.videoTitle}>
              <VideoFilled className={styles.videoTitleIcon} />
              <div>How to use AirAgent.ai?</div>
            </div>
            <div className={styles.videoContent}>
              <iframe
                width="255"
                height="145"
                src="https://www.youtube-nocookie.com/embed/KO4BKzmtpDk?si=Uy1swjS7qsxicFX8"
                title="YouTube video player"
                frameBorder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowFullScreen
                style={{
                  borderRadius: '8px',
                }}
              />
            </div>
          </div>
          <div className={styles.footerUsage}>
            <div className={styles.footerUsageItem}>
              <div>Current plan</div>
              <div className={styles.footerBadge}>Free</div>
            </div>
            <div className={styles.footerUsageItem}>
              <div>Agent</div>
              <div>4/5</div>
            </div>
            <div>
              <div className={styles.footerUsageItem}>
                <div>Message credit</div>
                <div>143/300</div>
              </div>
              <Progress percent={50} showInfo={false} strokeColor={colors.rainbowBrown3} strokeWidth={4} />
            </div>
            <div className={styles.footerUpgrade}>
              <a onClick={showPricingTableModal} className={styles.footerUpgradeText}>
                Upgrade
              </a>
            </div>
          </div>
        </div>
        <UserDropdown
          user={props.user}
          setTheme={(theme: ThemeName) => {
            props.setTheme(theme);
          }}
        />
      </div>
    </div>
  );
};

export default WorkspaceLeftSide;
