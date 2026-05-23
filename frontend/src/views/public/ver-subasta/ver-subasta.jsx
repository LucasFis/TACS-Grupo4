import styles from './ver-subasta.module.css'
import { useParams } from 'react-router'
import { useEffect, useState } from 'react'
import { buscarSubasta } from '../../../services/subastasService.js'
import Breadcrumb from '../../../components/ui/breadcrumb/breadcrumb.jsx'
import SectionCard from '../../../components/ui/section-card/section-card.jsx'
import SectionTitle from '../../../components/ui/section-title/section-title.jsx'
import PerfilSimple from '../../../components/ui/perfil-simple/perfil-simple.jsx'
import OfertaCard from './oferta-card.jsx'
import TuOfertaCard from './tu-oferta-card.jsx'
import Button from '../../../components/ui/button/button.jsx'
import useUsuarioActual from '../../../hooks/useUsuarioActual.js'
import { useNavigate } from 'react-router-dom'

const VerSubasta = () => {
  const { subId } = useParams()
  const { userId } = useUsuarioActual()
  const [cargando, setCargando] = useState(true)
  const [subasta, setSubasta] = useState(undefined)
  const [tiempo, setTiempo] = useState(0)
  const [subastaAbierta, setSubastaAbierta] = useState(false)
  const navigate = useNavigate()

  const procesarDuracion = () => {
    const horas = Math.floor(tiempo / 3600)
    const minutos = Math.floor((tiempo % 3600) / 60)
    const segundos = tiempo % 60

    return `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segundos.toString().padStart(2, '0')}`
  }

  const calcularDuracionTotal = () => {
    const inicio = new Date(subasta.inicio)
    const cierre = new Date(subasta.cierre)

    const diffMs = cierre - inicio

    const horas = Math.floor(diffMs / (1000 * 60 * 60))
    const minutos = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

    return `${horas}h ${minutos}m`
  }

  const formatearFecha = (fecha) => {
    const f = new Date(fecha)

    const dia = f.getDate()
    const mes = f.toLocaleString('es-AR', { month: 'short' }) // abr, may, etc
    const horas = f.getHours().toString().padStart(2, '0')
    const minutos = f.getMinutes().toString().padStart(2, '0')

    return `${dia} ${mes}, ${horas}:${minutos}`
  }

  const mostrarOfertaDeUsuario = (ofertas) => {
    const ofertaPropia = ofertas.find((o) => o.autor.usuario_id === userId.toString()) //Mismo Id que la sesion
    return ofertaPropia !== undefined ? (
      <TuOfertaCard oferta={ofertaPropia} subasta={subasta} subastaAbierta={subastaAbierta} />
    ) : (
      subastaAbierta && (
        <div className={'d-flex flex-row justify-content-center align-items-center gap-2'}>
          <p>¿Aún no ofertaste?</p>
          <Button onClick={() => navigate(`/subastas/${subId}/crear-oferta`)}>
            Proponer Oferta
          </Button>
        </div>
      )
    )
  }

  const cargarSubasta = async () => {
    try {
      setCargando(true)
      const payload = await buscarSubasta({ subId })
      setSubasta(payload)
      setSubastaAbierta(new Date(payload.cierre) > new Date())
      setTiempo(payload.tiempo_restante)
    } catch (err) {
      console.log(err)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargarSubasta()
  }, [])

  useEffect(() => {
    if (!tiempo) return

    const interval = setInterval(() => {
      setTiempo((prev) => Math.max(prev - 1, 0))
    }, 1000)

    return () => clearInterval(interval)
  }, [tiempo])

  const mostrarSubasta = () => {
    return (
      <>
        <div
          className={
            styles.figuritaSubastada +
            ' p-2 d-flex flex-column justify-content-center align-items-center gap-2 w-100 rounded-2 mb-3'
          }
        >
          <div className={styles.figuritaImagen + ' bg-white rounded-3 '}></div>

          <h4 className={'text-white'}>{subasta.figurita.jugador}</h4>
          <h6 className={'text-white'}>{subasta.figurita.seleccion}</h6>
        </div>
        <SectionCard>
          <SectionTitle>TIEMPO RESTANTE</SectionTitle>
          <SectionCard.Section>
            <div className="d-flex flex-row align-items-end gap-2">
              <h2>{procesarDuracion()}</h2>
              <p>HH:MM:SS</p>
            </div>
          </SectionCard.Section>
        </SectionCard>

        <SectionCard>
          <SectionTitle>DETALLES DE LA SUBASTA</SectionTitle>
          <SectionCard.Section>
            <div className="d-flex flex-column gap-3">
              {/* FILA 1 */}
              <div className="d-flex">
                <div className="w-50">
                  <h5>Inicio</h5>
                  <p>{formatearFecha(subasta.inicio)}</p>
                </div>
                <div className="w-50">
                  <h5>Cierre</h5>
                  <p>{formatearFecha(subasta.cierre)}</p>
                </div>
              </div>

              {/* FILA 2 */}
              <div className="d-flex">
                <div className="w-50">
                  <h5>Duración</h5>
                  <p>{calcularDuracionTotal()}</p>
                </div>
                <div className="w-50">
                  <h5>Ofertas recibidas</h5>
                  <p>{subasta.ofertas.length} ofertas</p>
                </div>
              </div>
            </div>
          </SectionCard.Section>
        </SectionCard>

        <SectionCard>
          <SectionTitle>PUBLICADO POR</SectionTitle>
          <SectionCard.Section>
            <PerfilSimple perfil={subasta.perfil} />
          </SectionCard.Section>
        </SectionCard>

        <SectionCard>
          <SectionTitle>CONDICIONES PARA OFERTAR</SectionTitle>
          <SectionCard.Section>
            <div className="d-flex flex-column gap-3">
              {/* Figuritas requeridas — siempre visible */}
              <div className="d-flex flex-column gap-2">
                <p
                  className="mb-0 text-uppercase fw-semibold text-muted"
                  style={{ fontSize: '0.72rem', letterSpacing: '0.06em' }}
                >
                  Figuritas requeridas
                </p>
                {subasta.figuritas_solicitadas.length > 0 ? (
                  <>
                    <div className="d-flex flex-column gap-2">
                      {subasta.figuritas_solicitadas.map((fig, i) => (
                        <div
                          key={i}
                          className="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                          style={{ backgroundColor: '#E1F5EE' }}
                        >
                          <div
                            className="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
                            style={{ width: 32, height: 32, backgroundColor: '#0F6E56' }}
                          >
                            <span style={{ fontSize: '0.7rem', fontWeight: 500, color: '#E1F5EE' }}>
                              {fig.numero}
                            </span>
                          </div>
                          <div>
                            <p
                              className="mb-0 fw-semibold"
                              style={{ fontSize: '0.85rem', color: '#085041' }}
                            >
                              {fig.jugador}
                            </p>
                            <p className="mb-0" style={{ fontSize: '0.72rem', color: '#0F6E56' }}>
                              {fig.seleccion}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                    <p className="mb-0 text-muted" style={{ fontSize: '0.72rem' }}>
                      El ofertante debe incluir al menos una de estas figuritas
                    </p>
                  </>
                ) : (
                  <p
                    className="mb-0 fst-italic"
                    style={{ fontSize: '0.85rem', color: 'var(--color-text-tertiary)' }}
                  >
                    Sin restricción — el ofertante puede ofrecer cualquier figurita
                  </p>
                )}
              </div>

              {/* Calificación mínima — siempre visible */}
              <div
                className="d-flex align-items-center justify-content-between pt-2"
                style={{ borderTop: '0.5px solid #9FE1CB' }}
              >
                <div>
                  <p
                    className="mb-0 text-uppercase fw-semibold text-muted"
                    style={{ fontSize: '0.72rem', letterSpacing: '0.06em' }}
                  >
                    Calificación mínima
                  </p>
                  <p className="mb-0 text-muted" style={{ fontSize: '0.72rem' }}>
                    {subasta.calificacion_minima_solicitada <= 1
                      ? 'Cualquier usuario puede ofertar'
                      : 'Solo usuarios con esta calificación pueden ofertar'}
                  </p>
                </div>
                {subasta.calificacion_minima_solicitada <= 1 ? (
                  <div
                    className="px-3 py-2 rounded-3 flex-shrink-0"
                    style={{
                      backgroundColor: 'var(--bs-secondary-bg)',
                      border: '0.5px solid var(--bs-border-color)',
                    }}
                  >
                    <span style={{ fontSize: '0.82rem', color: 'var(--bs-secondary-color)' }}>
                      Sin mínimo
                    </span>
                  </div>
                ) : (
                  <div
                    className="d-flex align-items-center gap-1 px-3 py-2 rounded-3 flex-shrink-0"
                    style={{ backgroundColor: '#E1F5EE' }}
                  >
                    <span className="fw-semibold" style={{ fontSize: '0.95rem', color: '#085041' }}>
                      {subasta.calificacion_minima_solicitada}
                    </span>
                    <span style={{ fontSize: '0.82rem', color: '#0F6E56' }}>★ o más</span>
                  </div>
                )}
              </div>
            </div>
          </SectionCard.Section>
        </SectionCard>

        <SectionCard>
          <SectionTitle>
            OFERTAS {subastaAbierta ? 'ACTUALES' : 'HISTORICAS'} ({subasta.ofertas.length})
          </SectionTitle>
          <SectionCard.Section>
            <div className="d-flex flex-column gap-2">
              {subasta.ofertas.length > 0 ? (
                subasta.ofertas.map((oferta, index) => (
                  <OfertaCard key={index} position={index + 1} propuesta={oferta} />
                ))
              ) : (
                <h4>Aún no hay ofertas!</h4>
              )}
            </div>
          </SectionCard.Section>
          {subasta.perfil.usuario_id !== userId && subastaAbierta ? (
            <>
              <SectionTitle>TU OFERTA</SectionTitle>
              <SectionCard.Section>{mostrarOfertaDeUsuario(subasta.ofertas)}</SectionCard.Section>
            </>
          ) : null}
        </SectionCard>
      </>
    )
  }

  return (
    <div className="container py-4 px-3 px-md-4">
      <Breadcrumb
        crumbs={[
          { name: 'Subasta', to: '/subastas' },
          { name: `#${subId}`, to: `/subastas/${subId}` },
        ]}
      />
      {cargando ? <h2>Cargando subasta...</h2> : mostrarSubasta()}
    </div>
  )
}

export default VerSubasta
