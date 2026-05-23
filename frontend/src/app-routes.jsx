import { Route, Routes } from 'react-router-dom'
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
import Administrador from './views/public/administrador/administrador.jsx'

const publics = [
  {
    path: '/',
    element: <Explorar />,
  },
  {
    path: '/explorar',
    element: <Explorar />,
  },
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/registrar',
    element: <Registrar />,
  },
  {
    path: '/acceso-denegado',
    element: <AccesoDenegado />,
  },
  {
    path: '/servidor-caido',
    element: <ServidorCaido />,
  },
  {
    path: '/error-interno',
    element: <ErrorInterno />,
  },
]

const privates = [
  {
    path: '/mis-figuritas',
    element: <MisFiguritas />,
  },
  {
    path: '/mis-figuritas/nueva-faltante',
    element: <NuevaFaltante />,
  },
  {
    path: '/mis-figuritas/nueva-repetida',
    element: <NuevaRepetida />,
  },
  {
    path: '/subastas/crear',
    element: <CrearSubasta />,
  },
  {
    path: '/sugerencias',
    element: <Sugerencias />,
  },
  {
    path: '/perfil',
    element: <Perfil />,
  },
  {
    path: '/intercambios',
    element: <Intercambios />,
  },
  {
    path: '/intercambios/crear',
    element: <CrearPropuestaIntercambio />,
  },
  {
    path: '/subastas/:subId',
    element: <VerSubasta />,
  },
  {
    path: '/subastas',
    element: <Subastas />,
  },
  {
    path: '/subastas/:subId/crear-oferta',
    element: <CrearOferta />,
  },
  {
    path: '/subastas/:subId/ofertas/:ofertaId/editar',
    element: <EditarOferta />,
  }
]

const AppRoutes = () => {
  return (
    <ErrorProvider>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            <Route element={<Layout />}>
              {publics.map((route) => (
                <Route key={route.path} path={route.path} element={route.element} />
              ))}

              <Route element={<RutaProtegida />}>
                {privates.map((route) => (
                  <Route key={route.path} path={route.path} element={route.element} />
                ))}
              </Route>
            </Route>
            {/* <Route path="*" element={<NotFound />} /> */}
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </ErrorProvider>
  )
}

export default AppRoutes
