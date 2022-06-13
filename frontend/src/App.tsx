import { ToastContainer } from "react-toastify";
import Submission from "./pages/Submission";
import 'react-toastify/dist/ReactToastify.css';
import Nav from "./components/Nav";
import Footer from "./components/Footer";
import Credits from "./pages/Credits";
import { Route, Routes } from "react-router-dom";
import { useState } from "react";

function App() {
  const [footerType, setFooterType] = useState<number>(1);

  return (
    <>
      <ToastContainer
        position="bottom-center"
        draggable={false}
      />


      <Nav />

      <Routes>
        <Route path="/" element={<Submission />} />
        <Route path="/credits" element={<Credits />} />
      </Routes>
    </>
  );
}

export default App;
