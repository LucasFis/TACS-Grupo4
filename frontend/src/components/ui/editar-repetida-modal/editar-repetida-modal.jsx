import { useState } from "react";

const METODOS = ["INTERCAMBIO", "SUBASTA"];

const EditarRepetidaModal = ({ figurita, onClose, onGuardar }) => {
  const [cantidadExistente, setCantidadExistente] = useState(
    figurita.cantidad_existente
  );

  const metodosActuales = figurita.metodos;

  const metodosDisponibles = METODOS.filter(
    (m) => !metodosActuales.includes(m)
  );

  const [nuevosMetodos, setNuevosMetodos] = useState([]);

  const toggleNuevoMetodo = (metodo) => {
    if (nuevosMetodos.includes(metodo)) {
      setNuevosMetodos(nuevosMetodos.filter((m) => m !== metodo));
    } else {
      setNuevosMetodos([...nuevosMetodos, metodo]);
    }
  };

  const handleGuardar = () => {
    const metodosFinales = [...metodosActuales, ...nuevosMetodos];
    const cantidadNueva = Number(cantidadExistente);

    const sinCambios =
      cantidadNueva === figurita.cantidad_existente &&
      metodosFinales.length === metodosActuales.length &&
      metodosFinales.every((m) => metodosActuales.includes(m));

    if (sinCambios) {
      onClose();
      return;
    }

    onGuardar({
      cantidadNueva,
      metodos: metodosFinales,
    });

    onClose();
  };

  return (
    <div
      className="modal d-block"
      style={{ backgroundColor: "rgba(0,0,0,0.4)" }}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content rounded-3">
          <div className="modal-header border-0 pb-0">
            <h6 className="modal-title fw-bold">
              Editar repetida ({figurita.jugador})
            </h6>
          </div>

          <div className="modal-body pt-2 d-flex flex-column gap-3">
            <div>
              <label
                className="form-label fw-semibold"
                style={{ fontSize: "0.9rem" }}
              >
                Cantidad existente
              </label>

              <input
                type="number"
                min={0}
                className="form-control"
                value={cantidadExistente}
                onChange={(e) => setCantidadExistente(e.target.value)}
                style={{ fontSize: "0.9rem" }}
              />
            </div>

            <div>
              <label
                className="form-label fw-semibold"
                style={{ fontSize: "0.9rem" }}
              >
                Métodos actuales
              </label>

              <div className="d-flex flex-wrap gap-2 mb-2">
                {metodosActuales.map((metodo) => (
                  <span
                    key={metodo}
                    className="badge rounded-pill"
                    style={{
                      backgroundColor: "#e1f5ee",
                      color: "#0f6e56",
                      fontSize: "0.85rem",
                      padding: "6px 12px",
                    }}
                  >
                    {metodo}
                  </span>
                ))}
              </div>
            </div>

            {metodosDisponibles.length > 0 && (
              <div>
                <label
                  className="form-label fw-semibold"
                  style={{ fontSize: "0.9rem" }}
                >
                  Agregar métodos
                </label>

                <div className="d-flex flex-column gap-2">
                  {metodosDisponibles.map((metodo) => (
                    <div key={metodo} className="form-check">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        checked={nuevosMetodos.includes(metodo)}
                        onChange={() => toggleNuevoMetodo(metodo)}
                      />

                      <label
                        className="form-check-label"
                        style={{ fontSize: "0.88rem" }}
                      >
                        {metodo}
                      </label>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="modal-footer border-0 pt-0 gap-2">
            <button
              className="btn btn-outline-secondary"
              style={{ fontSize: "0.85rem" }}
              onClick={onClose}
            >
              Cancelar
            </button>

            <button
              className="btn btn-success"
              style={{ fontSize: "0.85rem" }}
              onClick={handleGuardar}
            >
              Guardar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditarRepetidaModal;