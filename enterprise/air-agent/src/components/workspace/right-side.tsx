import { ChatPage } from 'modules/chat/chat_page';
import styles from './right-side.module.scss';

interface IWorkspaceRightSideProps {
  className: string;
  aiId: string | undefined;
}

const WorkspaceRightSide = (props: IWorkspaceRightSideProps) => {
  return <div className={props.className}>{props.aiId ? <ChatPage id={props.aiId} /> : <div>no</div>}</div>;
};

export default WorkspaceRightSide;
