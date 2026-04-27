import Button from "../../../components/ui/button/button";
import SectionCard from "../../../components/ui/section-card/section-card";
import SectionTitle from "../../../components/ui/section-title/section-title";
//import styles from './home.module.css';

const Home = () => {
  return (
    <main className="home">
      <SectionCard>
        <SectionCard.Section>
          <SectionTitle>Detalles de la subasta</SectionTitle>
          <p>Contenido...</p>
          <span>Más contenido</span>
        </SectionCard.Section>

        <SectionCard.Section>
          <SectionTitle>Publicada por</SectionTitle>
          <p>caro_r</p>
        </SectionCard.Section>

        <SectionCard.Section>
          <Button label="Ver subasta" />
          <Button label="Mejorar oferta" />
        </SectionCard.Section>
      </SectionCard>
    </main>
  );
};

export default Home;
