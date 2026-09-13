import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import MainLayout from './layouts/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';

import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import FinancialLearning from './pages/FinancialLearning';
import ArticleDetail from './pages/ArticleDetail';
import IncomeTax from './pages/IncomeTax';
import FindCA from './pages/FindCA';
import CaProfileDetail from './pages/CaProfileDetail';
import Calculators from './pages/Calculators';
import SearchResults from './pages/SearchResults';
import Dashboard from './pages/Dashboard';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <MainLayout>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route path="/learn" element={<FinancialLearning />} />
            <Route path="/learn/:slug" element={<ArticleDetail />} />
            <Route path="/income-tax" element={<IncomeTax />} />

            <Route path="/find-ca" element={<FindCA />} />
            <Route path="/find-ca/:id" element={<CaProfileDetail />} />

            <Route path="/calculators" element={<Calculators />} />
            <Route path="/search" element={<SearchResults />} />

            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />

            <Route path="*" element={<NotFound />} />
          </Routes>
        </MainLayout>
      </AuthProvider>
    </BrowserRouter>
  );
}
