import { Link, NavLink } from 'react-router-dom';
import './navbar.css';

const Navbar = () => {



  const NAV_LINKS = [
    { to: '/explorar',      label: 'Explorar' },
    { to: '/mis-figuritas', label: 'Mis figuritas' },
    { to: '/intercambios',  label: 'Intercambios' },
    { to: '/subastas',      label: 'Subastas' },
  ];

  return (
    <nav className="navbar navbar-expand-lg navbar-custom">
      <div className="container navbar-container">

        <Link className="navbar-brand navbar-logo" to="/">
          <span className="navbar-logo-icon">F</span>
          figuritas.app
        </Link>

        <div className="d-flex align-items-center gap-3 ms-auto d-lg-none">
          <button className="btn btn-link p-0 position-relative navbar-notification-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
              <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.995-14.901a1 1 0 1 0-1.99 0A5.002 5.002 0 0 0 3 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7a5.002 5.002 0 0 0-3.005-4.901z" />
            </svg>
            <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill navbar-badge">3</span>
          </button>

          <div className="rounded-circle d-flex align-items-center justify-content-center fw-bold navbar-avatar">
            MG
          </div>

          <button
            className="navbar-toggler border-0"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarContent"
          >
            <span className="navbar-toggler-icon navbar-toggler-custom" />
          </button>
        </div>

        <div className="collapse navbar-collapse" id="navbarContent">
          <ul className="navbar-nav mx-auto gap-2">
            {NAV_LINKS.map(({ to, label }) => (
              <li className="nav-item" key={to}>
                <NavLink
                  to={to}
                  className={({ isActive }) =>
                    `nav-link navbar-link ${isActive ? 'navbar-link--active' : ''}`
                  }
                >
                  {label}
                </NavLink>
              </li>
            ))}
          </ul>
        </div>

        <div className="d-none d-lg-flex align-items-center gap-3">
          <button className="btn btn-link p-0 position-relative navbar-notification-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
              <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.995-14.901a1 1 0 1 0-1.99 0A5.002 5.002 0 0 0 3 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7a5.002 5.002 0 0 0-3.005-4.901z" />
            </svg>

            {/* !!!Aca implementar logica para obtener cantidad de notificaciones pendientes sin hardcodear */}

            <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill navbar-badge">3</span>
          </button>

          <div className="rounded-circle d-flex align-items-center justify-content-center fw-bold navbar-avatar">
            MG
          </div>
        </div>

      </div>
    </nav>
  );
};

export default Navbar;