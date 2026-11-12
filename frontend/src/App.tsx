import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { ClientesPage } from './pages/ClientesPage';
import { ProductosPage } from './pages/ProductosPage';
import { NuevaFacturaPage } from './pages/NuevaFacturaPage';
import { FacturasPage } from './pages/FacturasPage';
import { FacturaDetallePage } from './pages/FacturaDetallePage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<DashboardPage />} />
          <Route path="clientes" element={<ClientesPage />} />
          <Route path="productos" element={<ProductosPage />} />
          <Route path="facturas/nueva" element={<NuevaFacturaPage />} />
          <Route path="facturas" element={<FacturasPage />} />
          <Route path="facturas/:id" element={<FacturaDetallePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default App;
