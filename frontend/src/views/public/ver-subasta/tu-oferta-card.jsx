import OfertaCard from "./oferta-card.jsx";
import Button from "../../../components/ui/button/button.jsx";
import {useNavigate, useParams} from "react-router";

const TuOfertaCard = ({oferta, subastaAbierta, subasta}) => {
    const navigate = useNavigate()
    const {subId} = useParams()

    return (
        <div className="d-flex flex-column gap-3">
            <OfertaCard propuesta={oferta} />
            {
                subastaAbierta &&
                    <>
                        <p>Podés mejorar tu oferta antes de que finalice!</p>
                        <Button onClick={() => navigate(`/subastas/${subId}/ofertas/${oferta.id}/editar`, { state: { oferta, subasta } })}>Editar oferta</Button>
                    </>
            }
        </div>
    )
}
export default TuOfertaCard