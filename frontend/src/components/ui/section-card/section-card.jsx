import styles from './section-card.module.css';

const SectionCard = ({ children }) => {
  const sections = Array.isArray(children) ? children : [children];

  return (
    <div className={styles.card}>
      {sections.map((section, i) => (
        <div key={i} className={styles.section}>
          {section}
        </div>
      ))}
    </div>
  );
};

export default SectionCard;