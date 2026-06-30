const ConfirmModal = ({
  show,
  titulo,
  mensaje,
  labelConfirmar,
  onConfirmar,
  onCancelar,
  advertencia,
}) => {
  if (!show) return null;

  return (
    <div
      className="modal d-block"
      style={{ backgroundColor: "rgba(0,0,0,0.4)" }}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content rounded-3">
          <div className="modal-header border-0 pb-0">
            <h6 className="modal-title fw-bold">{titulo}</h6>
          </div>
          <div className="modal-body pt-2">
            {advertencia && (
              <div
                className="alert alert-warning py-2 px-3 mb-3"
                style={{ fontSize: "0.85rem" }}
                role="alert"
              >
                {advertencia}
              </div>
            )}
            <p className="text-muted mb-0" style={{ fontSize: "0.9rem" }}>
              {mensaje}
            </p>
          </div>
          <div className="modal-footer border-0 pt-0 gap-2">
            <button
              className="btn btn-outline-secondary"
              style={{ fontSize: "0.85rem" }}
              onClick={onCancelar}
            >
              Cancelar
            </button>
            <button
              className="btn btn-success"
              style={{ fontSize: "0.85rem" }}
              onClick={onConfirmar}
            >
              {labelConfirmar}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
