import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/layouts/layout/layout'
import MisFiguritas from './views/public/mis-figuritas/mis-figuritas.jsx'
import NuevaFaltante from './views/public/nueva-faltante/nueva-faltante.jsx'
import NuevaRepetida from './views/public/nueva-repetida/nueva-repetida.jsx'
import Subastas from './views/public/subastas/subastas.jsx'
import CrearSubasta from './views/public/crear-subasta/crear-subasta.jsx'
import CrearPropuestaIntercambio from './views/public/crear-propuesta-intercambio/crear-propuesta-intercambio.jsx'
import Sugerencias from './views/public/sugerencias/sugerencias.jsx'
import VerSubasta from './views/public/ver-subasta/ver-subasta.jsx'
import Explorar from './views/public/explorar/explorar.jsx'
import Perfil from './views/public/perfil/perfil.jsx'
import Intercambios from './views/public/intercambios/intercambios.jsx'
import Notificaciones from './views/public/notificaciones/notificaciones.jsx'
import Login from '@/views/public/login/login.jsx'
import Registrar from '@/views/public/registrar/registrar.jsx'
import { AuthProvider } from '@/contexts/userContext.jsx'
import { ErrorProvider } from '@/contexts/errorContext.jsx'
import { ToastProvider } from '@/contexts/toastContext.jsx'
import AccesoDenegado from '@/views/public/errores/acceso-denegado/acceso-denegado.jsx'
import RutaProtegida from '@/components/autenticacion/ruta-protegida.jsx'
import ServidorCaido from '@/views/public/errores/servidor-caido/servidor-caido.jsx'
import ErrorInterno from '@/views/public/errores/error-interno/error-interno.jsx'
import CrearOferta from './views/public/crear-oferta/crear-oferta.jsx'
import EditarOferta from './views/public/editar-oferta/editar-oferta.jsx'
import VerIntercambio from './views/public/ver-intercambio/ver-intercambio.jsx'
import Administrador from './views/public/administrador/administrador.jsx'
import RutaConRol from '@/components/autenticacion/ruta-con-rol.jsx'
import VistaNoEncontrada from '@/views/public/errores/vista-no-encontrada/vista-no-encontrada.jsx'
import ErrorBoundary from '@/contexts/errorBoundary.jsx'

const rutasSinAdmin = [
  { path: '/', element: <Navigate to="/explorar" replace /> },
  { path: '/explorar', element: <Explorar /> },
]

const rutasPublicas = [
  { path: '/login', element: <Login /> },
  { path: '/registrar', element: <Registrar /> },
  { path: '/acceso-denegado', element: <AccesoDenegado /> },
  { path: '/servidor-caido', element: <ServidorCaido /> },
  { path: '/error-interno', element: <ErrorInterno /> },
]

const privadas = [
  { path: '/mis-figuritas', element: <MisFiguritas /> },
  { path: '/mis-figuritas/nueva-faltante', element: <NuevaFaltante /> },
  { path: '/mis-figuritas/nueva-repetida', element: <NuevaRepetida /> },
  { path: '/subastas/crear', element: <CrearSubasta /> },
  { path: '/sugerencias', element: <Sugerencias /> },
  { path: '/perfil', element: <Perfil /> },
  { path: '/intercambios', element: <Intercambios /> },
  { path: '/intercambios/crear', element: <CrearPropuestaIntercambio /> },
  { path: '/intercambios/:intercambioId', element: <VerIntercambio /> },
  { path: '/subastas/:subId', element: <VerSubasta /> },
  { path: '/subastas', element: <Subastas /> },
  { path: '/subastas/:subId/crear-oferta', element: <CrearOferta /> },
  { path: '/subastas/:subId/ofertas/:ofertaId/editar', element: <EditarOferta /> },
  { path: '/perfil/:perfilId', element: <Perfil /> },
  { path: '/notificaciones', element: <Notificaciones /> },
]

const privilegiadas = [
  { path: '/estadisticas', element: <Administrador /> },
]

const AppRoutes = () => {
  return (
    <ErrorBoundary>
      <ErrorProvider>
        <ToastProvider>
          <AuthProvider>
            <Routes>
              <Route element={<Layout />}>
                <Route element={<RutaConRol rol="ADMINISTRADOR" redirigirA="/estadisticas" invertir />}>
                  {rutasSinAdmin.map((route) => (
                    <Route key={route.path} path={route.path} element={route.element} />
                  ))}
                </Route>

                {rutasPublicas.map((route) => (
                  <Route key={route.path} path={route.path} element={route.element} />
                ))}

                <Route element={<RutaProtegida />}>
                  {privadas.map((route) => (
                    <Route key={route.path} path={route.path} element={route.element} />
                  ))}
                </Route>

                <Route element={<RutaConRol rol="ADMINISTRADOR" redirigirA="/acceso-denegado" />}>
                  {privilegiadas.map((route) => (
                    <Route key={route.path} path={route.path} element={route.element} />
                  ))}
                </Route>

                <Route path="*" element={<VistaNoEncontrada />} />
              </Route>
            </Routes>
          </AuthProvider>
        </ToastProvider>
      </ErrorProvider>
    </ErrorBoundary>
  )
}

export default AppRoutes
