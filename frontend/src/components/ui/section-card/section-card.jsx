import styles from './section-card.module.css';
import React from 'react';

const Section = ({ children }) => <>{children}</>;

const SectionCard = ({ children }) => {
  const sections = React.Children.toArray(children);

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

SectionCard.Section = Section;

export default SectionCard;