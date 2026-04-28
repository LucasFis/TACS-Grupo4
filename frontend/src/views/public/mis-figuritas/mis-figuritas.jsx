import { useState } from 'react';
import FiguritaTabs from '../../../components/ui/figurita-tabs/FiguritaTabs';
import SectionTitle from '../../../components/ui/section-title/section-title';
import Repetidas  from './tabs/Repetidas/Repetidas';
import Faltantes  from './tabs/Faltantes/Faltantes';
import styles from './MisFiguritas.module.css';

// ── Stats (podés fetchearlos por separado si vienen de otro endpoint) ──────────
const MOCK_STATS = {
  publicadas:  18,
  disponibles: 42,
  enSubasta:   5,
};

// ── Tab registry ──────────────────────────────────────────────────────────────
// Para agregar un nuevo tab: creá su componente y registralo acá.
// MisFiguritas no necesita saber nada del fetch interno de cada tab.
const TAB_COMPONENTS = {
  repetidas:  <Repetidas />,
  faltantes:  <Faltantes />,
  // propuestas: <Propuestas />,  ← cuando lo tengas listo
};

// ── Component ─────────────────────────────────────────────────────────────────
const MisFiguritas = () => {
  const [activeSection, setActiveSection] = useState('repetidas');

  return (
    <main className={styles.page}>
      <SectionTitle>Mis figuritas</SectionTitle>

      {/* Tabs + stats */}
      <FiguritaTabs
        activeTab={activeSection}
        onTabChange={setActiveSection}
        stats={MOCK_STATS}
      />

      {/* Contenido del tab activo */}
      <div className={styles.tabContent}>
        {TAB_COMPONENTS[activeSection]}
      </div>
    </main>
  );
};

export default MisFiguritas;
