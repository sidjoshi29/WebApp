// src/App.jsx

import './App.css';
import './styles/Header.css'; // Import Header.css after App.css
import './styles/Footer.css'; // Import Header.css after App.css
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/LayoutComponent';
import ListItemsComponent from './components/ListItemsComponent';
import ItemComponent from './components/ItemComponent';
import RegisterComponent from './components/RegisterComponent';
import LoginComponent from './components/LoginComponent';
import { isUserLoggedIn, isStaffUser, isAdminUser } from './services/AuthService';
import InventoryComponent from './components/InventoryComponent';
import ItemFormComponent from './components/ItemFormComponent';
import UserListComponent from './components/UserListComponent';
import AddStaffComponent from './components/AddStaffComponent';
import EditStaffComponent from './components/EditStaffComponent';
import EditCustomerComponent from './components/EditCustomerComponent';
import OrdersComponent from './components/OrdersComponent';
import OrderHistoryComponent from './components/OrderHistoryComponent';
import TaxRateComponent from './components/TaxRateComponent'
import MyOrdersComponent from './components/MyOrdersComponent';


function App() {
  function AuthenticatedRoute({ children }) {
    const isAuth = isUserLoggedIn();
    return isAuth ? children : <Navigate to='/login' />;
  }

  function StaffRoute({ children }) {
    const isAuth = isUserLoggedIn() && isStaffUser();
    return isAuth ? children : <Navigate to="/" />;
  }

  function AdminRoute({ children }) {
    const isAuth = isUserLoggedIn() && isAdminUser();
    return isAuth ? children : <Navigate to="/" />;
  }

  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path='/' element={<LoginComponent />} />
          <Route path='/register' element={<RegisterComponent />} />
          <Route path='/login' element={<LoginComponent />} />
          <Route path='/items' element={<AuthenticatedRoute><ListItemsComponent /></AuthenticatedRoute>} />
          <Route path="/item/:id" element={<AuthenticatedRoute><ItemComponent /></AuthenticatedRoute>} />
          <Route path="/add-item" element={<StaffRoute><ItemFormComponent /></StaffRoute>} />
          <Route path="/update-item/:id" element={<StaffRoute><ItemFormComponent /></StaffRoute>} />
          <Route path="/inventory" element={<StaffRoute><InventoryComponent /></StaffRoute>} />
          <Route path='/users' element={<AuthenticatedRoute><UserListComponent /></AuthenticatedRoute>} />
          <Route path='/add-staff' element={<AuthenticatedRoute><AddStaffComponent /></AuthenticatedRoute>} />
          <Route path='/edit-staff/:id' element={<AuthenticatedRoute><EditStaffComponent /></AuthenticatedRoute>} />
		  <Route path='/edit-customer/:id' element={<AuthenticatedRoute><EditCustomerComponent /></AuthenticatedRoute>} />
		  <Route path='/orders' element={<AuthenticatedRoute><OrdersComponent /></AuthenticatedRoute>} />
		  <Route path='/order-history' element={<StaffRoute><OrderHistoryComponent /></StaffRoute>} />
          <Route path='/tax-rate' element={<AuthenticatedRoute><TaxRateComponent /></AuthenticatedRoute>}></Route>
          <Route path="/my-orders" element={<AuthenticatedRoute><MyOrdersComponent /></AuthenticatedRoute>} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;