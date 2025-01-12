import styles from './style.module.less';

interface IProps {
  onClick?: (message: string) => boolean;
  content: string[];
}

export function MessageSuggestion(props: IProps) {
  const suggestions = props.content.filter((_) => _);
  if (!suggestions.length) return null;
  return (
    <div className={styles.suggestionContainer}>
      {suggestions.map((item, index) => (
        <div
          key={index}
          onClick={() => {
            if (props.onClick) {
              props.onClick(item);
            }
          }}
          className={styles.suggestionItem}
        >
          {item}
        </div>
      ))}
    </div>
  );
}
