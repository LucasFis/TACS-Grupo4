import Button from "../../../components/ui/button/button";
import Navbar from "../../../components/layouts/navbar/navbar"
import SectionCard from "../../../components/ui/section-card/section-card";
//import styles from './home.module.css';

const Home = () => {
  return (
    <main className="home">
        <Navbar />
        <Button label={"Tacas"}/>
        <SectionCard>
          <h1>Hola</h1>
          <h1>hola 2</h1>
          <h1>Hola 3</h1>
          <Button label={"boton"}/>
        </SectionCard>
        <SectionCard>
          <h1>Hola</h1>
          <h1>hola 2</h1>
          <h1>Hola 3</h1>
          <Button label={"boton"}/>
        </SectionCard>
        <SectionCard>
          <h1>Hola</h1>
          <h1>hola 2</h1>
          <h1>Hola 3</h1>
          <Button label={"boton"}/>
        </SectionCard>
        <SectionCard>
          <h1>Hola</h1>
          <h1>hola 2</h1>
          <h1>Hola 3</h1>
          <Button label={"boton"}/>
        </SectionCard>
    </main>
  );
};

export default Home;