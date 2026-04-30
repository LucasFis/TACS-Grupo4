import SectionTitle from '../../../components/ui/section-title/section-title';
import Repetidas  from './tabs/repetidas/repetidas';
import Faltantes  from './tabs/faltantes/faltantes';
import styles from './mis-figuritas.module.css';
import TabsContainer from "../../../components/ui/tabs-container/tabs-container.jsx";

const MisFiguritas = () => {

    const colId = "3"

    const TABS = [
        { key: 'repetidas', label: 'Repetidas', component: Repetidas, props: { colId } },
        { key: 'faltantes', label: 'Faltantes', component: Faltantes, props: { colId } },
    ];

  return (
    <main className={styles.page}>
      <SectionTitle>Mis figuritas</SectionTitle>
      <TabsContainer tabs={TABS} />
    </main>
  );
};

export default MisFiguritas;
