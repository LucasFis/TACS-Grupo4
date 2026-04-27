import styles from './section-title.module.css';

const SectionTitle = ({ children }) => {
  return <p className={styles.title}>{children}</p>;
};

export default SectionTitle;