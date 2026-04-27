import { Route, Routes } from 'react-router-dom';
import Home from "./views/public/home/home"

const publics = [
    {
        path: '/',
        element: <Home />,
    },
];

const privates = [];

const AppRoutes = () => {
  return (
    <Routes>
      {publics.map(route => (
        <Route key={route.path} path={route.path} element={route.element} />
      ))}
      {privates.map(route => (
        <Route key={route.path} path={route.path} element={route.element} />
      ))}
      {/* <Route path="*" element={<NotFound />} /> */}
    </Routes>
  );
};

export default AppRoutes;