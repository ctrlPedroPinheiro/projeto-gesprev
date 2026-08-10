import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import RotaProtegida from './routes/RotaProtegida';

const Login = lazy(() => import('./pages/Login/Login'));
const Dashboard = lazy(() => import('./pages/Dashboard/Dashboard'));
const Processos = lazy(() => import('./pages/Processos/Processos'));
const DetalheProcesso = lazy(() => import('./pages/Processos/DetalheProcesso'));
const Usuarios = lazy(() => import('./pages/Usuarios/Usuarios'));
const Chat = lazy(() => import('./pages/Chat/Chat'));

function CarregandoPagina() {
  return <p role="status">Carregando página...</p>;
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Suspense fallback={<CarregandoPagina />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={
              <RotaProtegida>
                <Dashboard />
              </RotaProtegida>
            } />
            <Route path="/processos" element={
              <RotaProtegida>
                <Processos />
              </RotaProtegida>
            } />
            <Route path="/processos/:id" element={
              <RotaProtegida>
                <DetalheProcesso />
              </RotaProtegida>
            } />
            <Route path="/usuarios" element={
              <RotaProtegida perfil="DIRETOR">
                <Usuarios />
              </RotaProtegida>
            } />
            <Route path="/chat" element={
              <RotaProtegida>
                <Chat />
              </RotaProtegida>
            } />
            <Route path="/" element={<Navigate to="/login" />} />
          </Routes>
        </Suspense>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
