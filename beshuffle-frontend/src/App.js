import React from 'react';
import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import PaginaAlbum from './pages/PaginaAlbum';
import './App.css';

function App() {
  return (
    <Router>
      <div className="container">
        <div className="card-container">
          <div className="header">
            <div className="logo">BeShuffle</div>
            <nav className="nav-menu">
              <NavLink to="/diario" className={({ isActive }) => isActive ? "subtitle nav-link active" : "subtitle nav-link"}>
                Álbum do Dia
              </NavLink>
              <NavLink to="/aleatorio" className={({ isActive }) => isActive ? "subtitle nav-link active" : "subtitle nav-link"}>
                Aleatório
              </NavLink>
            </nav>
          </div>

          <div id="content">
            <Routes>
              <Route path="/diario" element={<PaginaAlbum tipo="diario" />} />
              <Route path="/aleatorio" element={<PaginaAlbum tipo="aleatorio" />} />
              <Route path="*" element={<PaginaAlbum tipo="diario" />} />
            </Routes>
          </div>
        </div>
      </div>
    </Router>
  );
}

export default App;