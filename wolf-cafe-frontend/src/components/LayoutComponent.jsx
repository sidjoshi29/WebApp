// src/components/Layout.jsx

import React from 'react';
import HeaderComponent from './HeaderComponent';
import FooterComponent from './FooterComponent';
import '../styles/Layout.css';

/**
 * Layout: A wrapper component for the application's overall layout structure.
 */
const Layout = ({ children }) => {
  return (
    <div className="layout">
      <HeaderComponent />
      <main className="main-content">{children}</main>
      <FooterComponent />
    </div>
  );
};

export default Layout;
